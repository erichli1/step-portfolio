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

/**
 * Adds a random greeting to the page.
 */
function addRandomQuote() {
  const quotes =
      ['[Human\'s] great skill is their capacity to escalate. (The Book Thief)', 'As flies to wanton boys, are we to the gods, they kill us for sport. (Gloucester in King Lear)', 'When we are headed the wrong way, the last thing we need is progress. (Nick Bostrom)', 'Things don\'t have to be perfect to be wonderful. (NYTimes The Best Advice You\'ve Ever Received)'];

  // Pick a random greeting.
  const quote = quotes[Math.floor(Math.random() * quotes.length)];

  // Add it to the page.
  const quoteContainer = document.getElementById('quote-container');
  quoteContainer.innerText = quote;
}
