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

import java.util.LinkedHashMap;
import java.util.Scanner;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/donations-data")
public class DonationsDataServlet extends HttpServlet {

  private LinkedHashMap<String, Integer> donations = new LinkedHashMap<>();
  private static final int MAX_CAUSES = 8;

  @Override
    public void init() {
        Scanner scanner = new Scanner(getServletContext().getResourceAsStream("/WEB-INF/donations-sorted.csv"));

        int counter = 0;

        while (scanner.hasNextLine() && counter < MAX_CAUSES) {
            String line = scanner.nextLine();
            String[] cells = line.split(",");

            String cause = cells[1];
            Integer amount = Integer.valueOf(cells[2]);

            donations.put(cause, amount);
            counter++;
        }

        scanner.close();

    }
  
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        response.setContentType("application/json");

        Gson gson = new Gson();
        String json = gson.toJson(donations);
        response.getWriter().println(json);
    }
}
