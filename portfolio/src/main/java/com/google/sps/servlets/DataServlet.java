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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

@WebServlet("/data")
public class DataServlet extends HttpServlet {

    private static final String REQUEST_PARAMETER_COMMENT_INPUT = "comment-input";
    private static final String REQUEST_PARAMETER_PICTURE_LINK = "picture-link";
    private static final String REQUEST_PARAMETER_NUMBER_COMMENTS = "number-comments";
    private static final String REQUEST_PARAMETER_NAME = "name";
    private static final String REDIRECT_COMMENTS = "/comments.html";

    // Print json containing the content of each card
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ArrayList<Comment> comments = new ArrayList<>();

        int numberComments = 0;

        // Get the positive integer of max comments to show
        int maxNumberComments = getPositiveInt(request);

        // Default to 10 comments if number is not valid
        if(maxNumberComments == -1) {
            maxNumberComments = 10;
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        
        Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
        PreparedQuery results = datastore.prepare(query);
        for (Entity entity : results.asIterable()) {
            
            String name = (String) entity.getProperty("name");
            String email = (String) entity.getProperty("email");
            String message = (String) entity.getProperty("message");
            String pictureLink = (String) entity.getProperty("picture");
            long timestamp = (long) entity.getProperty("timestamp");

            Comment comment = new Comment(name, email, message, pictureLink, timestamp);
            
            comments.add(comment);

            numberComments++;
            if (numberComments == maxNumberComments) {
                break;
            }
        }

        Gson gson = new Gson();
        String json = gson.toJson(comments);
        
        response.setContentType("application/json");
        response.getWriter().println(json);
    }

    // Add a comment to the datastore
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String message = request.getParameter(REQUEST_PARAMETER_COMMENT_INPUT);
        String pictureLinkRaw = request.getParameter(REQUEST_PARAMETER_PICTURE_LINK);
        String name = request.getParameter(REQUEST_PARAMETER_NAME);
        String email = getEmail();

        String pictureLink = getPictureLink(pictureLinkRaw);

        long timestamp = System.currentTimeMillis();

        Entity commentEntity = new Entity("Comment");
        commentEntity.setProperty("name", name);
        commentEntity.setProperty("email", email);
        commentEntity.setProperty("message", message);
        commentEntity.setProperty("timestamp", timestamp);
        commentEntity.setProperty("picture", pictureLink);


        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(commentEntity);


        response.sendRedirect(REDIRECT_COMMENTS);
    }

    // Return the current user's email
    private String getEmail() {
        UserService userService = UserServiceFactory.getUserService();

        return userService.getCurrentUser().getEmail();
    }

    // Format and return the picture link from Autodraw
    private String getPictureLink(String pictureLinkRaw) {
        int startOfId = pictureLinkRaw.lastIndexOf("/") + 1;
        
        // Check to make sure the string is in bounds
        if(startOfId != 0 && startOfId <= pictureLinkRaw.length()) {
            String pictureLink = "https://storage.googleapis.com/artlab-public.appspot.com/share/" + pictureLinkRaw.substring(startOfId) + ".png";
            return pictureLink;
        }
        else {
            return "";
        }

    }

    // Convert the entered number of comments to show into a positive integer and return -1 if cannot convert or is negative
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