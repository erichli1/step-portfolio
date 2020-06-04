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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static final String REQUEST_PARAMETER_COMMENT_INPUT = "comment-input";
  private static final String REQUEST_PARAMETER_NUMBER_COMMENTS = "number-comments";
  private static final String REDIRECT_COMMENTS = "/comments.html";

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ArrayList<String> messages = new ArrayList<>();

    int numberComments = 0;
    int maxNumberComments = getPositiveInt(request);

    if(maxNumberComments == -1) {
        maxNumberComments = 10;
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
        String message = (String) entity.getProperty("message");
        messages.add(message);
        numberComments++;
        if (numberComments == maxNumberComments) {
            break;
        }
    }

    Gson gson = new Gson();
    String json = gson.toJson(messages);
    
    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String message = request.getParameter(REQUEST_PARAMETER_COMMENT_INPUT);
    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("message", message);
    commentEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);


    response.sendRedirect(REDIRECT_COMMENTS);
  }

  private int getPositiveInt(HttpServletRequest request) {
    String numberInputStr = request.getParameter(REQUEST_PARAMETER_NUMBER_COMMENTS);
    int numberInputInt;

    try {
        numberInputInt = Integer.parseInt(numberInputStr);
    } catch (NumberFormatException e) {
        System.err.println("Could not convert to int: " + numberInputStr);
        return -1;
    }

    if(numberInputInt <= 0) {
        System.err.println("Input number is not a positive integer");
        return -1;
    }

    return numberInputInt;
    
  }
}