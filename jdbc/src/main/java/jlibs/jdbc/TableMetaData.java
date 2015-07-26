/**
 * Copyright 2015 Santhosh Kumar Tekuri
 *
 * The JLibs authors license this file to you under the Apache License,
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

package jlibs.jdbc;

/**
 * @author Santhosh Kumar T
 */
public class TableMetaData{
    public final String name;
    public final ColumnMetaData columns[];
    public final int autoColumn;
    public TableMetaData(String name, ColumnMetaData... columns){
        this.name = name;
        this.columns = columns;

        int auto = -1;
        for(int i=0; i<columns.length; i++){
            if(columns[i].auto){
                auto = i;
                break;
            }
        }
        autoColumn = auto;
    }

    public int getColumnIndex(String property){
        for(int i=columns.length-1; i>=0; i--){
            if(columns[i].property.equals(property))
                return i;
        }
        return -1;
    }
}
