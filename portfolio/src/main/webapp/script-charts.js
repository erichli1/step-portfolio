google.charts.load('current', {'packages':['bar', 'timeline']});
google.charts.setOnLoadCallback(drawTimeline);
google.charts.setOnLoadCallback(drawDonationGraph);
google.charts.setOnLoadCallback(drawProgramGraph);

/** Creates a chart and adds it to the page. */
function drawTimeline() {

    const date = new Date();
    const today = new Date(date.getFullYear(), date.getMonth(), date.getDate());

    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Order');
    data.addColumn('string', 'Position')
    data.addColumn('date', 'Start');
    data.addColumn('date', 'End');
        data.addRows([
            ['1', 'Intern at Boston Housing Authority', new Date(2020, 2, 20), new Date(2020, 4, 18)],
            ['1', 'Google STEP Intern', new Date(2020, 4, 18), today],
            ['2', 'Founder, Head of HCS for Social Good', new Date(2020, 4, 30), today],
            ['3', 'CTO of Global Research and Consulting', new Date(2020, 4, 3), today],
            ['4', 'Operations and Engagement Lead of Harvard Effective Altruism', new Date(2020, 4, 31), today],
        ]);

    var options = {
        timeline: { showRowLabels: false }
    };

    const chart = new google.visualization.Timeline(document.getElementById('timeline-container'));
    chart.draw(data, options);
}

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