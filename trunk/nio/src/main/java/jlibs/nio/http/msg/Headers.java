/*
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package jlibs.nio.http.msg;

import jlibs.nio.Reactor;
import jlibs.nio.http.expr.ValueMap;
import jlibs.nio.http.util.Parser;
import jlibs.nio.http.util.USAscii;

import java.util.*;
import java.util.function.Function;

import static jlibs.nio.http.util.USAscii.QUOTE;

/**
 * @author Santhosh Kumar Tekuri
 */
public final class Headers implements ValueMap{
    private Object table[] = new Object[16];
    private Header first;

    /*-------------------------------------------------[ Get ]---------------------------------------------------*/

    public Header getFirst(){ return first; }

    public Header get(AsciiString name){
        return entry(name, false);
    }

    public Header get(CharSequence name){
        return entry(name, USAscii.caseInsensitiveHashCode(name), false);
    }

    public String value(AsciiString name){
        Header header = get(name);
        return header==null ? null : header.value;
    }

    public String value(CharSequence name){
        Header header = get(name);
        return header==null ? null : header.value;
    }

    /*-------------------------------------------------[ Add ]---------------------------------------------------*/

    public void add(AsciiString name, String value){
        if(name==null || value==null)
            return;
        Header head = entry(name, true);
        if(head.value==null)
            head.value = value;
        else{
            Header newHeader = newHeader(name);
            newHeader.value = value;
            Header tail = head.samePrev;
            tail.sameNext = newHeader;
            newHeader.samePrev = tail;
            head.samePrev = newHeader;
        }
        assert validateLinks();
    }

    /*-------------------------------------------------[ Remove ]---------------------------------------------------*/

    public Header remove(AsciiString name){
        if(name==null || first==null)
            return null;

        Header h = null;
        int idx = name.hashCode() & (table.length-1);
        Object obj = table[idx];
        if(obj==null)
            return null;
        else if(obj instanceof Header){
            Header header = (Header)obj;
            if(header.name.equals(name)){
                table[idx] = null;
                h = header;
            }
        }else{
            Header headers[] = (Header[])obj;
            for(int i=0; i<headers.length; i++){
                Header header = headers[i];
                if(header!=null && header.name.equals(name)){
                    headers[i] = null;
                    h = header;
                    break;
                }
            }
        }

        if(h!=null)
            removeSameNext(h);
        return h;
    }

    private void removeSameNext(Header header){
        while(header!=null){
            if(header==first){
                first = header.next;
                if(first!=null)
                    first.prev = header.prev;
            }else{
                Header before = header.prev;
                Header after = header.next;
                before.next = after;
                if(after==null)
                    first.prev = before;
                else
                    after.prev = before;
            }
            header.prev = header;
            header.next = null;
            header = header.sameNext;
        }
        assert validateLinks();
    }

    public void clear(){
        Arrays.fill(table, null);
        first = null;
    }

    /*-------------------------------------------------[ Set ]---------------------------------------------------*/

    public void set(AsciiString name, String value){
        if(name==null)
            return;
        if(value==null){
            remove(name);
            return;
        }
        Header head = entry(name, true);
        if(head.value==null)
            head.value = value;
        else{
            head.value = value;
            Header next = head.sameNext;
            head.sameNext = null;
            head.samePrev = head;
            removeSameNext(next);
        }
        assert validateLinks();
    }

    /*-------------------------------------------------[ Internal-Helpers ]---------------------------------------------------*/

    private Header newHeader(AsciiString name){
        Header header = new Header(name);
        if(first==null)
            first = header;
        else{
            header.prev = first.prev;
            first.prev.next = header;
            first.prev = header;
        }
        return header;
    }

    private Header entry(AsciiString name, boolean create){
        int idx = name.hashCode() & (table.length-1);
        Object obj = table[idx];
        if(obj==null){
            if(create){
                Header header = newHeader(name);
                table[idx] = header;
                return header;
            }else
                return null;
        }else if(obj instanceof Header){
            Header header = (Header)obj;
            if(header.name.equals(name))
                return header;
            if(create){
                Header newHeader = newHeader(name);
                Header headers[] = { header, newHeader, null, null };
                table[idx] = headers;
                return newHeader;
            }else
                return null;
        }else{
            Header headers[] = (Header[])obj;
            int freeSlot = -1;
            for(int i=0; i<headers.length; i++){
                Header header = headers[i];
                if(header==null){
                    if(freeSlot==-1)
                        freeSlot = i;
                }else if(header.name.equals(name))
                    return header;
            }
            if(create){
                Header newHeader = newHeader(name);
                if(freeSlot==-1){
                    int len = headers.length;
                    headers = Arrays.copyOf(headers, len+3);
                    table[idx] = headers;
                    headers[len] = newHeader;
                }else
                    headers[freeSlot] = newHeader;
                return newHeader;
            }else
                return null;
        }
    }

    Header entry(CharSequence name, int hashCode, boolean create){
        int idx = hashCode & (table.length-1);
        Object obj = table[idx];
        if(obj==null){
            if(create){
                Header header = newHeader(new AsciiString(name, hashCode));
                table[idx] = header;
                return header;
            }else
                return null;
        }else if(obj instanceof Header){
            Header header = (Header)obj;
            if(header.name.hashCode()==hashCode && header.name.equals(name))
                return header;
            if(create){
                Header newHeader = newHeader(new AsciiString(name, hashCode));
                Header headers[] = { header, newHeader, null, null };
                table[idx] = headers;
                return newHeader;
            }else
                return null;
        }else{
            Header headers[] = (Header[])obj;
            int freeSlot = -1;
            for(int i=0; i<headers.length; i++){
                Header header = headers[i];
                if(header==null){
                    if(freeSlot==-1)
                        freeSlot = i;
                }else if(header.name.hashCode()==hashCode && header.name.equals(name))
                    return header;
            }
            if(create){
                Header newHeader = newHeader(new AsciiString(name, hashCode));
                if(freeSlot==-1){
                    int len = headers.length;
                    headers = Arrays.copyOf(headers, len+3);
                    table[idx] = headers;
                    headers[len] = newHeader;
                }else
                    headers[freeSlot] = newHeader;
                return newHeader;
            }else
                return null;
        }
    }

    private boolean validateLinks(){
        Header h1 = first;
        int count1 = 0;
        while(h1!=null){
            if(h1.next==null)
                assert first.prev==h1;
            else
                assert h1.next.prev==h1;
            h1 = h1.next;
            ++count1;
        }

        int count2 = 0;
        for(Object entry: table){
            if(entry!=null){
                Header headers[];
                if(entry instanceof Header)
                    headers = new Header[]{ (Header)entry };
                else
                    headers = (Header[])entry;
                for(Header h2: headers){
                    Header t = h2;
                    while(h2!=null){
                        if(h2.sameNext==null)
                            assert t.samePrev==h2;
                        else
                            assert h2.sameNext.samePrev==h2;
                        h2 = h2.sameNext;
                        ++count2;
                    }
                }
            }
        }

        return count1==count2;
    }

    /*-------------------------------------------------[ Value-Map ]---------------------------------------------------*/

    @Override
    public Object getValue(String name){
        return value(name);
    }

    /*-------------------------------------------------[ HTTP-Helpers ]---------------------------------------------------*/

    @Override
    public String toString(){
        StringBuilder buffer = Reactor.stringBuilder();
        Header header = first;
        while(header!=null){
            buffer.append(header.name).append(": ").append(header.value).append("\r\n");
            header = header.next;
        }
        buffer.append("\r\n");
        return Reactor.free(buffer);
    }

    public <T> T getSingleValue(AsciiString name, Function<String, T> delegate){
        String value = value(name);
        if(value==null)
            return null;
        if(value.length()>0 && value.charAt(0)==QUOTE)
            value = new Parser(false, value).value();
        return delegate.apply(value);
    }

    public <T> void setSingleValue(AsciiString name, T value, Function<T, String> delegate){
        if(value==null)
            remove(name);
        else
            set(name, delegate==null ? value.toString() : delegate.apply(value));
    }

    public <T> List<T> getListValue(AsciiString name, Function<Parser, T> delegate, boolean foldable){
        Parser parser = null;
        List<T> list = null;

        Header header = get(name);
        while(header!=null){
            if(parser==null)
                parser = new Parser(foldable, header.getValue());
            else
                parser.reset(header.getValue());
            if(!parser.isEmpty()){
                if(list==null)
                    list = new ArrayList<>();
                while(true){
                    list.add(delegate.apply(parser));
                    if(parser.isEmpty())
                        break;
                    else
                        parser.skip();
                }
            }
            header = header.sameNext();
        }
        if(list==null)
            list = Collections.emptyList();
        return list;
    }

    public <T> void setListValue(AsciiString name, Collection<T> values, Function<T, String> delegate, boolean foldable){
        if(values==null || values.isEmpty())
            remove(name);
        else if(values.size()==1){
            T value = values.iterator().next();
            String hvalue = delegate==null ? value.toString() : delegate.apply(value);
            set(name, hvalue);
        }else if(foldable){
            StringBuilder builder = new StringBuilder();
            for(T value: values){
                if(builder.length()>0)
                    builder.append(',').append(' ');
                builder.append(delegate==null ? value.toString() : delegate.apply(value));
            }
            set(name, builder.toString());
        }else{
            remove(name);
            for(T value: values)
                add(name, delegate==null ? value.toString() : delegate.apply(value));
        }
    }

    public <T> Map<String, T> getMapValue(AsciiString name, Function<Parser, T> delegate, Function<T, String> nameFunction, boolean foldable){
        Parser parser = null;
        Map<String, T> map = null;

        Header header = get(name);
        while(header!=null){
            if(parser==null)
                parser = new Parser(foldable, header.getValue());
            else
                parser.reset(header.getValue());
            if(!parser.isEmpty()){
                if(map==null)
                    map = new HashMap<>();
                while(true){
                    T item = delegate.apply(parser);
                    map.put(nameFunction.apply(item), item);
                    if(parser.isEmpty())
                        break;
                    else
                        parser.skip();
                }
            }
            header = header.sameNext();
        }
        if(map==null)
            map = Collections.emptyMap();
        return map;
    }

    public <T> void setMapValues(AsciiString name, Map<String, T> map, Function<T, String> delegate, boolean foldable){
        setListValue(name, map.values(), delegate, foldable);
    }
}
