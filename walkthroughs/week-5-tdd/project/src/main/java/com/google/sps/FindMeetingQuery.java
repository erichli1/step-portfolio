// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

public final class FindMeetingQuery {
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        long duration = request.getDuration();
        Collection<String> requestedAttendees = request.getAttendees();
        Collection<String> optionalAttendees = request.getOptionalAttendees();

        boolean mandatoryFlag = false;
        ArrayList<TimeRange> busyTimeRanges = new ArrayList<TimeRange>();
        ArrayList<TimeRange> optionalBusyTimeRanges = new ArrayList<TimeRange>();

        for(Event event : events) {
            Collection<String> eventAttendees = event.getAttendees();
            TimeRange eventTimeRange = event.getWhen();

            // check if there are people who can't make it
            if(!Collections.disjoint(requestedAttendees, eventAttendees)) {
                // mark that there is at least one event by a mandatory attendee
                mandatoryFlag = true;

                busyTimeRanges.add(eventTimeRange);
                optionalBusyTimeRanges.add(eventTimeRange);
            }
            // adds to optional events list if includes optional events
            else if(!Collections.disjoint(optionalAttendees, eventAttendees)) {
                optionalBusyTimeRanges.add(eventTimeRange);
            }
            else {
                // do nothing
            }
        }

        // merges and sorts the busy time ranges
        busyTimeRanges = updateBusyTimeRanges(busyTimeRanges);
        optionalBusyTimeRanges = updateBusyTimeRanges(optionalBusyTimeRanges);

        ArrayList<TimeRange> freeTimeRanges = getFreeTimeRanges(busyTimeRanges, duration);
        ArrayList<TimeRange> optionalFreeTimeRanges = getFreeTimeRanges(optionalBusyTimeRanges, duration);
        
        // uses the optional free time ranges if there are free options for mandatory & optional attendees OR were no events for mandatory attendee
        if(optionalFreeTimeRanges.size() != 0 || !mandatoryFlag) {
            return optionalFreeTimeRanges;
        }
        else {
            return freeTimeRanges;
        }
    }

    // returns list of free time ranges in a day given list of busy times
    private ArrayList<TimeRange> getFreeTimeRanges(ArrayList<TimeRange> busyTimeRanges, long duration) {
        ArrayList<TimeRange> freeTimeRanges = new ArrayList<TimeRange>();

        int startTime = TimeRange.START_OF_DAY;

        // busy time ranges should be sorted by start time
        for(TimeRange busyTimeRange : busyTimeRanges) {
            TimeRange timeRange = TimeRange.fromStartEnd(startTime, busyTimeRange.start(), false);
            if(longEnough(timeRange, duration)) {
                freeTimeRanges.add(timeRange);
            }
            startTime = busyTimeRange.end();
        }       

        // adds the last chunk of the day in because there's no event at the end of the day
        TimeRange lastTimeRange = TimeRange.fromStartEnd(startTime, TimeRange.END_OF_DAY, true);
        if(longEnough(lastTimeRange, duration)) {
            freeTimeRanges.add(lastTimeRange);
        }

        return freeTimeRanges;
    }

    // merges and sorts the time ranges into disjoint time ranges
    private ArrayList<TimeRange> updateBusyTimeRanges(ArrayList<TimeRange> busyTimeRanges) {
        // sort time ranges by start time
        Collections.sort(busyTimeRanges, TimeRange.ORDER_BY_START);

        int index = 0;
        while(index < busyTimeRanges.size() - 1) {
            TimeRange currBusyTimeRange = busyTimeRanges.get(index);
            TimeRange nextBusyTimeRange = busyTimeRanges.get(index + 1);
            
            // if overlap, combine current and next and put index on merged
            if(currBusyTimeRange.overlaps(nextBusyTimeRange)) {
                TimeRange mergedBusyTimeRange = mergeTimeRanges(currBusyTimeRange, nextBusyTimeRange);
                busyTimeRanges.set(index, mergedBusyTimeRange);
                busyTimeRanges.remove(index + 1);
            }
            // if no overlap, move index to next
            else {
                // moves the index to compare next with next next
                index++;
            }
        }

        return busyTimeRanges;
    }

    // merges two time ranges that overlap
    private TimeRange mergeTimeRanges(TimeRange currBusyTimeRange, TimeRange nextBusyTimeRange) {
        // current contains next
        if (currBusyTimeRange.contains(nextBusyTimeRange)) {
            return currBusyTimeRange;
        }
        // next contains current
        else if (nextBusyTimeRange.contains(currBusyTimeRange)) {
            return nextBusyTimeRange;
        }
        // pure overlap, currBusy will start before nextBusy because they've been sorted by start time in updateBusyTimeRanges()
        else {
            TimeRange mergedBusyTimeRange = TimeRange.fromStartEnd(currBusyTimeRange.start(), nextBusyTimeRange.end(), false);
            return mergedBusyTimeRange;
        }
    }

    // returns true if this time period satisfies the duration requirement
    private boolean longEnough (TimeRange timeRange, long duration) {
        if (timeRange.duration() >= (int) duration) {
            return true;
        }
        else {
            return false;
        }
    }
}
