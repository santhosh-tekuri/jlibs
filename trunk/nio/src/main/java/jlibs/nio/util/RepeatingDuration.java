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

package jlibs.nio.util;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * @author Santhosh
 */
public enum RepeatingDuration{
    HOURLY{
        protected long next(ZonedDateTime zdt){
            return  zdt.plusHours(1)
                    .withMinute(0).withSecond(0).withNano(0)
                    .toInstant().toEpochMilli();
        }
    },
    DAILY{
        protected long next(ZonedDateTime zdt){
            return zdt.plusDays(1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0)
                    .toInstant().toEpochMilli();
        }
    },
    WEEKLY{
        protected long next(ZonedDateTime zdt){
            return zdt.plusWeeks(1).with(DayOfWeek.SUNDAY)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0)
                    .toInstant().toEpochMilli();
        }
    },
    MONTHLY{
        protected long next(ZonedDateTime zdt){
            return zdt.with(TemporalAdjusters.firstDayOfNextMonth())
                    .withHour(0).withMinute(0).withSecond(0).withNano(0)
                    .toInstant().toEpochMilli();
        }
    },
    YEARLY{
        protected long next(ZonedDateTime zdt){
            return zdt.with(TemporalAdjusters.firstDayOfNextYear())
                    .withHour(0).withMinute(0).withSecond(0).withNano(0)
                    .toInstant().toEpochMilli();
        }
    };

    protected abstract long next(ZonedDateTime zdt);

    public long next(long from){
        return next(ZonedDateTime.ofInstant(Instant.ofEpochMilli(from), Clock.systemDefaultZone().getZone()));
    }

    public long next(){
        return next(ZonedDateTime.now());
    }

    public static RepeatingDuration forFormat(String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = new Date();
        String valueNow = sdf.format(date);

        for(RepeatingDuration rd: values()){
            if(!sdf.format(new Date(rd.next(date.getTime()))).equals(valueNow))
                return rd;
        }
        return null;
    }
}
