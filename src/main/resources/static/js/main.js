'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var topicInput = document.querySelector('#topics-selector');
var messageArea = document.querySelector('#messageArea');
var rankingArea = document.querySelector('#rankingArea');
var connectingElement = document.querySelector('.connecting');

var stompClient = null;
var username = null;
var previusTopic = null;
var publicTopic = 0;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if(username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/chat');
        stompClient = Stomp.over(socket);
        
        stompClient.connect({}, function () {
			console.log('### Connected');	
			subscribeToChatMessage(publicTopic);
			fillTopicSelect();
			registerUser(publicTopic);
			connectingElement.classList.add('hidden');
			getLast10MessagesFromTopic(publicTopic);
			sendMessageToServer('Joining', 'JOIN', topicInput.value);
			messageInput.focus()
		});
    }
    event.preventDefault();
}

function disconnect() {
   if (stompClient !== null) {
       stompClient.disconnect(function() {
           console.log("Client disconnected");
       });
       stompClient = null;
   }
}

function subscribeToChatMessage(topicId){
	stompClient.subscribe('/topic/chat/sended/' + topicId, function (payload) {
		console.log('### Subscribed to topic ' + topicId);
		onMessageReceived(payload);
	});
}

function unSubscribeAll(){
	for (const sub in stompClient.subscriptions) {
	  if (stompClient.subscriptions.hasOwnProperty(sub)) {
	  	stompClient.unsubscribe(sub);
	  }
	}
}

function fillTopicSelect() {
	stompClient.subscribe('/app/chat/topics/subscribe', function (response) {
	    var topics = JSON.parse(response.body);
		var select = document.querySelector('#topics-selector');
		
		for (var i = 0; i < topics.length; i += 1) {
		    var option = document.createElement("option");
	  		option.text = topics[i];
	  		option.value = i;
	  		select.add(option);
		}
    });
}

function getLast10MessagesFromTopic(topicId) {
	stompClient.subscribe('/app/chat/messages/subscribe/' + topicId, onMessageReceived);
}

function send(event) {
	var messageContent = messageInput.value.trim();

    sendMessageToServer(messageContent, 'CHAT', topicInput.value);
    
    messageInput.value = '';

	event.preventDefault();
}

function sendMessageToServer(message, type, topicValue){
	if(message && stompClient) {
		
		var chatMessage = {
            user: username,
            content: message,
            type: type,
            topicId: topicValue
        }; 

        stompClient.send("/app/chat/send/" + topicValue, {}, JSON.stringify(chatMessage));
	}
}

function registerUser(topicId){
    stompClient.send("/app/chat/register/" + topicId, {},
        JSON.stringify({user: username, type: 'JOIN'})
    )
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
    
    
	if(message.length > 0){
		for (var i = 0; i < message.length; i += 1) {
			writeMessage(message[i]);	
			writeRanking(message[i]);    
    	}
	} else {
		writeMessage(message);
		writeRanking(message);  
	}
	
    sortRanking();
	
}

function writeMessage(message){
	if(message === undefined || message.length === 0 ){
		return;
	}
	
	var messageElement = document.createElement('li');
	
    if(message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.user + ' joined the ' + topicInput.options[topicInput.selectedIndex].text + ' topic';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.user + ' left!';
    } else {
        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.user[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.user);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.user);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
    
}


function sortRanking() {
    var list = document.querySelector("#rankingArea");
    var liList = list.getElementsByTagName("li");
    var sorted;
    var i;
    for (i = 0; i < liList.length - 1; i++) {
		var a = Number(liList[i].dataset.index)
        var b = Number(liList[i + 1].dataset.index)
        
        if ( a < b ) {
        	sorted = true;
            break;
        }
	}
      
	if (sorted) {
		liList[i].parentNode.insertBefore(liList[i + 1], liList[i]);
	}
}

function writeRanking(message){
	if(message === undefined || message.length === 0){
		return;
	}
	
	var userRankElement = document.querySelector('#p-'+message.user);
	
	if(userRankElement){
		//document exists
		userRankElement.innerText = message.score;
		var liRanking = document.querySelector('#li-' + message.user);
		liRanking.setAttribute("data-index", message.score);
		
	} else {
	
		var rankingElement = document.createElement('li');
		rankingElement.setAttribute("data-index", message.score);
		rankingElement.setAttribute("id", 'li-' + message.user);
	 
        rankingElement.classList.add('user-ranking');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.user[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.user);

        rankingElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.user);
        usernameElement.appendChild(usernameText);
        rankingElement.appendChild(usernameElement);
	    
	
	    var textElement = document.createElement('p');
	    textElement.setAttribute("id", 'p-' + message.user);
	    var messageText = document.createTextNode(message.score);
	    textElement.appendChild(messageText);
	
	    rankingElement.appendChild(textElement);
	
	    rankingArea.appendChild(rankingElement);
	    rankingArea.scrollTop = rankingArea.scrollHeight;
	    
	}
    
}

function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }

    var index = Math.abs(hash % colors.length);
    return colors[index];
}

function cleanChatArea() {
	 messageArea.innerHTML = "";
}

function onChangeTopic() {
	unSubscribeAll(); 
	sendMessageToServer('Bye', 'LEAVE', previusTopic);
	
	cleanChatArea();	
	connectingElement.classList.remove('hidden');
	
	subscribeToChatMessage(topicInput.value);
	getLast10MessagesFromTopic(topicInput.value);
	sendMessageToServer('Joining', 'JOIN', topicInput.value);
	
	connectingElement.classList.add('hidden');
}

function storeValue(){
	previusTopic = topicInput.value;
}

usernameForm.addEventListener('submit', connect, true);
messageForm.addEventListener('submit', send, true);
topicInput.addEventListener('change', onChangeTopic, true);
topicInput.addEventListener('focus', storeValue, true);
