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

package jlibs.nio.http.msg.spec;

import jlibs.nio.http.msg.Message;
import jlibs.nio.http.msg.Version;
import jlibs.nio.http.msg.spec.values.MediaType;
import jlibs.nio.http.msg.spec.values.QualityItem;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santhosh Kumar Tekuri
 */
public class Accept extends ListHeaderSpec<QualityItem<MediaType>>{
    public Accept(){
        super("Accept");
    }

    @Override
    protected QualityItem<MediaType> parseSingle(Parser parser, Version version){
        String name = parser.lvalue();
        int slash = name.indexOf('/');
        String type = name.substring(0, slash);
        String subType = name.substring(slash+1);
        parser.rvalue();

        double quality = 1;
        Map<String, String> params = null;
        while(true){
            String paramName = parser.lvalue();
            if(paramName==null)
                break;
            if(QualityItem.QUALITY.equals(paramName))
                quality = Double.parseDouble(parser.rvalue());
            else{
                if(params==null)
                    params = new HashMap<>();
                params.put(paramName, parser.rvalue());
            }
        }
        return new QualityItem<>(new MediaType(type, subType, params), quality);
    }

    @Override
    public String formatSingle(QualityItem<MediaType> value, Version version){
        return value==null ? null : value.toString();
    }

    @Override
    public List<QualityItem<MediaType>> get(Message message){
        List<QualityItem<MediaType>> mediaTypes = super.get(message);
        mediaTypes.sort(COMPARATOR);
        return mediaTypes;
    }

    private static final Comparator<QualityItem<MediaType>> COMPARATOR = new Comparator<QualityItem<MediaType>>(){
        private int score(MediaType mt){
            if("*".equals(mt.type))
                return 0;
            if("*".equals(mt.subType))
                return 1;
            else
                return 2;
        }

        @Override
        public int compare(QualityItem<MediaType> o1, QualityItem<MediaType> o2){
            int score1 = score(o1.item);
            int score2 = score(o2.item);
            if(score1==score2){
                if(o1.item.type.equals(o2.item.type) && o1.item.subType.equals(o2.item.subType)){
                    int paramScore1 = o1.item.params.size();
                    int paramScore2 = o2.item.params.size();
                    if(paramScore1==paramScore2)
                        return Double.compare(o2.quality, o1.quality);
                    else
                        return Integer.compare(paramScore2, paramScore1);
                }else
                    return -1;
            }else
                return Integer.compare(score2, score1);
        }
    };

    public static double getQuality(MediaType mt, List<QualityItem<MediaType>> accept){
        for(QualityItem<MediaType> qualityItem: accept){
            if(mt.isCompatible(qualityItem.item)){
                boolean paramsMatched = true;
                for(Map.Entry<String, String> param: qualityItem.item.params.entrySet()){
                    String value = mt.params.get(param.getKey());
                    if(!param.getValue().equals(value)){
                        paramsMatched = false;
                        break;
                    }
                }
                if(paramsMatched)
                    return qualityItem.quality;
            }
        }
        return 0;
    }
}
