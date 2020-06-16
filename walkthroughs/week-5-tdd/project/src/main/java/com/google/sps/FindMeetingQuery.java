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
        // throw new UnsupportedOperationException("TODO: Implement this method.");

        ArrayList<TimeRange> acceptedTimeRanges = new ArrayList<TimeRange>();
        long duration = request.getDuration();
        Collection<String> requestedAttendees = request.getAttendees();

        for(Event event : events) {
            Collection<String> eventAttendees = event.getAttendees();

            // check if there are people who can't make it
            if(!Collections.disjoint(requestedAttendees, eventAttendees)) {
                TimeRange eventTimeRange = event.getWhen();
                
                for(int index = 0; index < acceptedTimeRanges.size(); index++) {
                    TimeRange acceptedTimeRange = acceptedTimeRanges.get(index);

                    ArrayList<TimeRange> splicedAcceptedTimeRanges = splice(acceptedTimeRange, eventTimeRange, duration);

                    // add the new accepted times to the list in front of the outdated accepted time range
                    // should theoretically preserve time ordering of acceptedTimeRanges
                    for (TimeRange splicedAcceptedTimeRange : splicedAcceptedTimeRanges) {
                        acceptedTimeRanges.add(index, splicedAcceptedTimeRange);
                        index++;
                    }

                    acceptedTimeRanges.remove(index);

                    // index here points to the next element, subtract 1 to account for adding 1 at the end of the loop
                    index--;
                }
                
            }
        }

        return acceptedTimeRanges;
    }

    // returns the new spliced accepted time range after accounting for the event conflicts
    private ArrayList<TimeRange> splice(TimeRange acceptedTimeRange, TimeRange eventTimeRange, long duration) {

        ArrayList<TimeRange> newAcceptedTimeRanges = new ArrayList<TimeRange>();

        if (acceptedTimeRange.overlaps(eventTimeRange)) {
            // accounts for when event includes accepted (as well as when equals)
            if (eventTimeRange.contains(acceptedTimeRange)) {
                newAcceptedTimeRanges = spliceContainsAccepted(acceptedTimeRange, eventTimeRange, duration);
            }
            else if (acceptedTimeRange.contains(eventTimeRange)) {
                newAcceptedTimeRanges = spliceContainsEvent(acceptedTimeRange, eventTimeRange, duration);
            }
            else {
                newAcceptedTimeRanges = spliceOverlap(acceptedTimeRange, eventTimeRange, duration);
            }
        }
        else {
            newAcceptedTimeRanges.add(acceptedTimeRange);
        }

        return newAcceptedTimeRanges;
    }

    // splice time if event contains accepted
    private ArrayList<TimeRange> spliceContainsAccepted (TimeRange acceptedTimeRange, TimeRange eventTimeRange, long duration) {
        ArrayList<TimeRange> newAcceptedTimeRanges = new ArrayList<TimeRange>();

        return newAcceptedTimeRanges;
    }


    // splice time if accepted contains event
    private ArrayList<TimeRange> spliceContainsEvent(TimeRange acceptedTimeRange, TimeRange eventTimeRange, long duration) {
        ArrayList<TimeRange> newAcceptedTimeRanges = new ArrayList<TimeRange>();
        
        int acceptedTimeRangeStart = acceptedTimeRange.start();
        int eventTimeRangeStart = eventTimeRange.start();
        int acceptedTimeRangeEnd = acceptedTimeRange.end();
        int eventTimeRangeEnd = eventTimeRange.end();

        int gap1 = eventTimeRangeStart - acceptedTimeRangeStart;
        TimeRange newAcceptedTimeRange1 = TimeRange.fromStartDuration(acceptedTimeRangeStart, gap1);
        if(longEnough(newAcceptedTimeRange1, duration)) {
            newAcceptedTimeRanges.add(newAcceptedTimeRange1);
        }

        int gap2 = acceptedTimeRangeEnd - eventTimeRangeEnd;
        TimeRange newAcceptedTimeRange2 = TimeRange.fromStartDuration(eventTimeRangeEnd, gap2);
        if(longEnough(newAcceptedTimeRange2, duration)) {
            newAcceptedTimeRanges.add(newAcceptedTimeRange2);
        }

        return newAcceptedTimeRanges;
    }

    // splice time if just overlaps, doesn't satisfy contain
    private ArrayList<TimeRange> spliceOverlap(TimeRange acceptedTimeRange, TimeRange eventTimeRange, long duration) {
        ArrayList<TimeRange> newAcceptedTimeRanges = new ArrayList<TimeRange>();
        
        int acceptedTimeRangeStart = acceptedTimeRange.start();
        int eventTimeRangeStart = eventTimeRange.start();

        // only need to account for < or > because == would satisfy contains
        if (acceptedTimeRangeStart < eventTimeRangeStart) {
            int gap = eventTimeRangeStart - acceptedTimeRangeStart;
            TimeRange newAcceptedTimeRange = TimeRange.fromStartDuration(acceptedTimeRangeStart, gap);

            if(longEnough(newAcceptedTimeRange, duration)) {
                newAcceptedTimeRanges.add(newAcceptedTimeRange);
            }
        }
        else {
            int acceptedTimeRangeEnd = acceptedTimeRange.end();
            int eventTimeRangeEnd = eventTimeRange.end();

            int gap = acceptedTimeRangeEnd - eventTimeRangeEnd;
            TimeRange newAcceptedTimeRange = TimeRange.fromStartDuration(eventTimeRangeEnd, gap);

            if(longEnough(newAcceptedTimeRange, duration)) {
                newAcceptedTimeRanges.add(newAcceptedTimeRange);
            }
        }

        return newAcceptedTimeRanges;
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
