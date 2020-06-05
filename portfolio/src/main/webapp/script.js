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
 * Adds a random quote to the page.
 */
var lastNum = 0;

function addRandomQuote() {
  const quotes =
      ['The best time to plant a tree is 20 years ago, the second best time is today.', 'Human\'s great skill is their capacity to escalate.', 'As flies to wanton boys, are we to the gods, they kill us for sport.', 'When we are headed the wrong way, the last thing we need is progress.', 'Things don\'t have to be perfect to be wonderful.'];

  // Pick a random quote.
  num = Math.floor(Math.random() * quotes.length);

  // check if the last quote is this one
  while(num == lastNum) {
      num = Math.floor(Math.random() * quotes.length);
  }
  const quote = quotes[Math.floor(Math.random() * quotes.length)];

  lastNum = num;

  // Add it to the page.
  const quoteContainer = document.getElementById('quote-container');
  quoteContainer.innerText = quote;
}

function seeMore(id) {

    const moreId = id + 'More';
    const lessId = id + 'Less';
    const gifId = id + '-vid';
    const gifLoc = '/images/' + id +'-vid.gif';

    document.getElementById(lessId).style.display = 'none';
    document.getElementById(moreId).style.display = 'block';
    document.getElementById(gifId).src = gifLoc;

}

function seeLess(id) {

    const moreId = id + 'More';
    const lessId = id + 'Less';

    document.getElementById(lessId).style.display = 'block';
    document.getElementById(moreId).style.display = 'none';
}

function getComments() {

    var numberComments = document.getElementById("number-comments").value;
    var fetchURL = '/data?number-comments=' + numberComments;

    fetch(fetchURL).then(response => response.json()).then((comments) => {
        const commentsList = document.getElementById('comments-container');

        commentsList.innerHTML = '';
        
        for(comment of comments) {
            commentsList.appendChild(createCommentCard(comment.name, comment.message, comment.pictureLink));
        }
    });
}

function deleteComments() {
    const request = new Request('/delete-data', {method: 'POST'});

    fetch(request).then(response => {
        getComments();
    });
}

function createListElement(message) {
    const listElement = document.createElement('li');
    listElement.innerText = message;
    return listElement;
}

function createCommentCard(name, message, pictureLink) {
    var commentCard = document.getElementById("cardCommentTemplate").cloneNode(true);
    commentCard.style.display = "block";

    name = sanitizeString(name);
    message = sanitizeString(message);

    commentCard.getElementsByTagName("img")[0].src = pictureLink;
    commentCard.getElementsByTagName("p")[0].innerHTML = name;
    commentCard.getElementsByTagName("p")[1].innerHTML = message;

    return commentCard;
}

function sanitizeString(string) {
    string = string.replace(/</g, "&lt;").replace(/>/g, "&gt;");

    return string;
}