<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<%@ page import="com.mongodb.MongoClient" %>
<%@ page import="com.mongodb.client.MongoDatabase" %>

<%@ page import="org.bson.types.ObjectId" %>
	
<%@ page import="com.therapy.servlets.Util" %>
<%@ page import="com.therapy.entities.*" %>

<%
	MongoClient mongoClient = Util.getMongoClient();
	MongoDatabase database = mongoClient.getDatabase(Util.DATABASE_NAME);
	
	Therapist therapist = new Therapist((ObjectId)session.getAttribute("id"), database);
	Chat[] chats = therapist.getChats();
		
	session.setAttribute("isNewMessagingSession", true);
	session.removeAttribute("chat");
	
%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="ISO-8859-1">
	<title>Insert title here</title>
	<script src="https://code.jquery.com/jquery-1.10.2.js"></script>
	<link rel="stylesheet" type="text/css" href="therapistHomepage.css">
</head>
<body>
	
	<!-- Messaging -->

	<%if(chats != null) { %>
	
		<form id="goToMessenger" method="get" action="/CulminatingVer8/getChat">
			<%for(int i = 0; i < chats.length; i++) { %>
				<input type="submit" name="patientChats" value="<%=chats[i].toString()%>">
			<%}%>
		</form> 
		
	<%}%>
	
	<!-- Requests -->
	
	<form id="goToRequests" action="therapistRequests.jsp">
		<input type="submit" value="Requests">
	</form>
	
	
</body>
</html>