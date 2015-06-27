/**
 * Copyright 2015 The JLibs Project
 *
 * The JLibs Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package jlibs.wadl.cli.completors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santhosh Kumar T
 */
public class Buffer{
    private String str;
    private int fromPrev = 0;
    private int from = 0;
    private List<String> candidates;

    public Buffer(String str, int cursor, List<String> candidates){
        this.str = str.substring(0, cursor);
        this.candidates = candidates;
    }

    private int separatorIndex;
    private String arg;
    private List<String> args = new ArrayList<String>();
    
    public String next(){
        fromPrev = from;
        separatorIndex = str.indexOf(' ', from);
        if(separatorIndex==-1)
            arg = str.substring(from);
        else{
            arg = str.substring(from, separatorIndex);
            from = separatorIndex;
            while(str.charAt(from)==' '){
                from++;
                if(from==str.length())
                    break;
            }
        }
        args.add(arg);
        return arg;
    }

    public String arg(int i){
        return args.get(i);
    }
    
    public int getFrom(){
        return candidates.isEmpty() ? -1 : from;
    }

    public void eat(int count){
        fromPrev += count;
        from = fromPrev;
        arg = arg.substring(count);
    }

    public boolean hasNext(){
        return separatorIndex!=-1;
    }
    
    public void addCandidate(String candidate){
        addCandidate(candidate, ' ');
    }

    public void addCandidate(String candidate, char separator){
        if(candidate.startsWith(arg)){
            if(separator==0)
                candidates.add(candidate);
            else
                candidates.add(candidate+separator);
        }
    }

    public void addCandidateIgnoreCase(String candidate){
        if(candidate.toLowerCase().startsWith(arg.toLowerCase()))
            candidates.add(candidate+' ');
    }
    
    public boolean hasCandidates(){
        return !candidates.isEmpty();
    }

    public List<String> candidates(){
        return candidates;
    }
}
