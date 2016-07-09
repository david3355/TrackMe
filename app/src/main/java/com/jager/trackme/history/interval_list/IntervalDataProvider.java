package com.jager.trackme.history.interval_list;

import com.jager.trackme.history.HistoryInterval;
import com.jager.trackme.history.Unit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Jager on 2016.07.08..
 */
public class IntervalDataProvider
{
       public static HashMap<String, List<HistoryInterval>> getInfo()
       {
              HashMap<String, List<HistoryInterval>> intervalSelector = new HashMap<>();
              List<HistoryInterval> intervals = new ArrayList<>();
              intervals.add(new HistoryInterval(5, Unit.MINUTE));
              intervals.add(new HistoryInterval(10, Unit.MINUTE));
              intervals.add(new HistoryInterval(30, Unit.MINUTE));
              intervals.add(new HistoryInterval(1, Unit.HOUR));
              intervals.add(new HistoryInterval(6, Unit.HOUR));
              intervals.add(new HistoryInterval(12, Unit.HOUR));
              intervals.add(new HistoryInterval(1, Unit.DAY));
              intervals.add(new HistoryInterval(7, Unit.DAY));
              intervals.add(new HistoryInterval(15, Unit.DAY));
              intervals.add(new HistoryInterval(1, Unit.MONTH));
              intervals.add(new HistoryInterval(3, Unit.MONTH));
              intervals.add(new HistoryInterval(6, Unit.MONTH));
              intervals.add(new HistoryInterval(1, Unit.YEAR));

              intervalSelector.put("Choose positions from the last ...", intervals);
              return intervalSelector;
       }

}
