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

// Store the displayed quote index
var lastNum = 0;

// Generate a random quote from list of quotes
function addRandomQuote() {
    const quotes =
        ['The best time to plant a tree is 20 years ago, the second best time is today.', 'Human\'s great skill is their capacity to escalate.', 'As flies to wanton boys, are we to the gods, they kill us for sport.', 'When we are headed the wrong way, the last thing we need is progress.', 'Things don\'t have to be perfect to be wonderful.'];

    num = Math.floor(Math.random() * quotes.length);

    // Check if this quote is already displayed
    while(num == lastNum) {
        num = Math.floor(Math.random() * quotes.length);
    }
    const quote = quotes[Math.floor(Math.random() * quotes.length)];

    lastNum = num;

    const quoteContainer = document.getElementById('quote-container');
    quoteContainer.innerText = quote;
}

// Show the full card of a "My work" section card
function seeMore(id) {

    const moreId = id + 'More';
    const lessId = id + 'Less';
    const gifId = id + '-vid';
    const gifLoc = '/images/' + id +'-vid.gif';

    document.getElementById(lessId).style.display = 'none';
    document.getElementById(moreId).style.display = 'block';
    document.getElementById(gifId).src = gifLoc;

}

// Hide the full card of a "My work" section card
function seeLess(id) {

    const moreId = id + 'More';
    const lessId = id + 'Less';

    document.getElementById(lessId).style.display = 'block';
    document.getElementById(moreId).style.display = 'none';
}

// Show the form and get the comments
function onLoad() {
    showForm();
    getComments();
}

// Get the comments
function getComments() {

    var numberComments = document.getElementById('number-comments').value;
    var fetchURL = '/data?number-comments=' + numberComments;

    fetch(fetchURL).then(response => response.json()).then((comments) => {
        const commentsList = document.getElementById('comments-container');

        // Reset the comments to be empty on each call to get fresh comments
        commentsList.innerHTML = '';
        
        for(comment of comments) {
            commentsList.appendChild(createCommentCard(comment.name, comment.message, comment.pictureLink, comment.timestamp));
        }
    });
}

// Show the form if logged in
function showForm() {
    fetch('/login').then(response => response.json()).then((login) => {
        if (login.loggedIn) {
            document.getElementById('login-text').style.display = 'none';
            document.getElementById('logout-text').style.display= 'block';
            document.getElementById('logout-link').href = login.redirectLink;
            document.getElementById('comment-form').style.display = 'block';
            document.getElementById('autodraw-iframe').src = "https://www.autodraw.com/";
        }
        else {
            document.getElementById('login-text').style.display = 'block';
            document.getElementById('logout-text').style.display = 'none';
            document.getElementById('login-link').href = login.redirectLink;
            document.getElementyById('autodraw-iframe').src = "";
            document.getElementById('comment-form').style.display = 'none';
        }
    });

}

// Delete all comments for everybody
function deleteComments() {
    const request = new Request('/delete-data', {method: 'POST'});

    fetch(request).then(response => {
        getComments();
    });
}

// Create and return a comment card given the name, comment, picture, and time
function createCommentCard(name, message, pictureLink, timestamp) {
    // Get the comment card template from comments.html
    var commentCard = document.getElementById("cardCommentTemplate").cloneNode(true);

    name = sanitizeString(name);
    message = sanitizeString(message);

    var time = new Date(timestamp);

    commentCard.getElementsByTagName("img")[0].src = pictureLink;
    commentCard.getElementsByTagName("p")[0].innerHTML = name;
    commentCard.getElementsByTagName("p")[1].innerHTML = message;
    commentCard.getElementsByTagName("p")[2].innerHTML = time;

    commentCard.style.display = "block";

    return commentCard;
}

// Sanitize the string to avoid HTML injection
function sanitizeString(string) {
    string = string.replace(/</g, "&lt;").replace(/>/g, "&gt;");

    return string;
}