google.charts.load('current', {'packages':['bar', 'timeline']});
google.charts.setOnLoadCallback(drawTimeline);
google.charts.setOnLoadCallback(drawDonationGraph);
google.charts.setOnLoadCallback(drawProgramGraph);
google.charts.setOnLoadCallback(drawAnswerGraph);

// Boolean indicator of whether answer should be shown
var answerShown = false;

// Draw timeline of my experiences using hardcoded data
function drawTimeline() {

    const date = new Date();
    const today = new Date(date.getFullYear(), date.getMonth(), date.getDate());

    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Order');
    data.addColumn('string', 'Position')
    data.addColumn('date', 'Start');
    data.addColumn('date', 'End');
    data.addRows([
        ['Row 1', 'Intern at Boston Housing Authority', new Date(2020, 2, 20), new Date(2020, 4, 18)],
        ['Row 1', 'Google STEP Intern', new Date(2020, 4, 18), today],
        ['Row 2', 'Founder, Head of HCS for Social Good', new Date(2020, 4, 30), today],
        ['Row 3', 'CTO of Global Research and Consulting', new Date(2020, 4, 3), today],
        ['Row 4', 'Operations and Engagement Lead of Harvard Effective Altruism', new Date(2020, 4, 31), today],
    ]);
    
    var options = {
        timeline: { showRowLabels: false }
    };

    const chart = new google.visualization.Timeline(document.getElementById('timeline-container'));
    chart.draw(data, options);
}

// Show the most donated to causes by pulling CSV data
function drawDonationGraph() {
    fetch('/donations-data').then(response => response.json()).then((donations) => {
        const data = new google.visualization.DataTable();
        data.addColumn('string', 'Cause');
        data.addColumn('number', 'Amount donated');
        Object.keys(donations).forEach((cause) => {
            data.addRow([cause, donations[cause]]);
        })

        const chart = new google.charts.Bar(document.getElementById('donation-container'));
        
        chart.draw(data);
    });
}

// Show the votes on which program is the most effective
function drawProgramGraph() {
    fetch('/charity-data').then(response => response.json()).then((charities) => {
        const data = new google.visualization.DataTable();
        data.addColumn('string', 'Charity');
        data.addColumn('number', 'Votes');
        
        charities.forEach((charity) => {
            data.addRow([charity.program, charity.votes]);
        })

        const chart = new google.charts.Bar(document.getElementById('charity-container'));

        chart.draw(data);
    })
}

// Show the efficacy analysis graph using hardcoded data
function drawAnswerGraph() {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Charity');
    data.addColumn('number', 'Additional Years Per $100');
    
    data.addRows([
        ['Conditional cash transfers', 0.1],
        ['Merit scholarships', 0.3],
        ['Free uniforms', 0.7],
        ['Educating parents', 21]
    ]);

    // Show the element so the graph can be generated correctly
    document.getElementById('answerCard').style.display = 'block';

    const chart = new google.charts.Bar(document.getElementById('answer-container'));
    
    // Hide the graph once generated
    google.visualization.events.addListener(chart, 'ready', function() {
        document.getElementById('answerCard').style.display = 'none';
    })

    chart.draw(data);
}

// Show/hide the correct answer and graph
function showAnswer() {
    answerShown = !answerShown;

    if (answerShown) {
        document.getElementById('showAnswerButton').innerHTML = 'Hide answer';
        document.getElementById('answerCard').style.display = 'block';
    }
    else {
        document.getElementById('showAnswerButton').innerHTML = 'Show answer';
        document.getElementById('answerCard').style.display = 'none';
    }
}