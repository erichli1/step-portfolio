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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.sps.data.Charity;
import java.util.ArrayList;
import java.util.Scanner;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/charity-data")
public class CharityDataServlet extends HttpServlet {

    private static final String REDIRECT_CHARTS = "/charts.html";

    // Create an array list of the current number of votes for each program and print as json
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        response.setContentType("application/json");

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("Program");

        PreparedQuery results = datastore.prepare(query);

        ArrayList<Charity> programVotes = new ArrayList<>();

        for(Entity entity: results.asIterable()) {
            String program = (String) entity.getProperty("program");
            long votes = (long) entity.getProperty("votes");

            Charity charity = new Charity(program, votes);

            programVotes.add(charity);
        }

        Gson gson = new Gson();
        String json = gson.toJson(programVotes);
        response.getWriter().println(json);
    }

    // Add a vote to the selected program
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String charity = request.getParameter("gridRadios");

        // Check if program was selected
        if (charity != null) {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

            Key charityKey = KeyFactory.createKey("Program", charity);

            Entity programVote;
            
            // Check if selected program already has votes
            try {
                programVote = datastore.get(charityKey);
            } catch (EntityNotFoundException e) {
                programVote = new Entity("Program", charity);
                programVote.setProperty("program", charity);
                programVote.setProperty("votes", (long) 0);
            }

            long currentVotes = (long) programVote.getProperty("votes");

            // Check to make sure adding one won't cause wraparound to negative
            if (currentVotes != Long.MAX_VALUE) {
                programVote.setProperty("votes", currentVotes + 1);
                datastore.put(programVote);
            }
        }
        
        response.sendRedirect(REDIRECT_CHARTS);
    }
}
