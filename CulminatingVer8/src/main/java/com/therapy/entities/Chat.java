package com.therapy.entities;


import static com.mongodb.client.model.Filters.eq;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;

/**
 * This class represents a chat between a patient and therapist. It has fields
 * to identify the patient and therapist that it belongs to, as well as a
 * <code>List</code> that contains all of the messages sent between users. 
 *
 * @author Yousef Bulbulia
 * 
 */
public class Chat extends Entity{
	 
    private Patient patient;
    private Therapist therapist;
    private Deque<Message> messages = new ArrayDeque<Message>();
    
    /**
     * Creates a new <code>Chat</code> with it's messages being created by reading
     * them in from the database.
     * 
     * @param patient   the patient using this chat
     * @param therapist the therapist using this chat
     * @param id        the _id field that this chat will use
     * @param database  the database this chat belongs to
     */
    public Chat(Patient patient, Therapist therapist, ObjectId id, MongoDatabase database) {
    	
    	super(id, database);
    	
    	this.patient = patient;
    	this.therapist = therapist;
    	
    	MongoCollection<Document> collection = database.getCollection("chats");
    	
    	Document chatDoc = collection.find(eq(id)).first();
    	
    	ArrayList<ObjectId> messageIdList = chatDoc.get("message_ids", new ArrayList<ObjectId>().getClass());
    	ObjectId[] messageIds = messageIdList.toArray(ObjectId[]::new);
    	
    	for(ObjectId messageId : messageIds) {
    		
    		Message message = new Message(patient, therapist, messageId, database);
    		messages.add(message);
    	}
    	
    }
    
    /**
     * Creates a new <code>Chat</code> with a unique _id field, and no messages. 
     * After creation, the <code>Chat</code> is inserted into the <code>chats</code> 
     * collection.
     * 
     * @param patient   the patient using this chat
     * @param therapist the therapist using this chat
     * @param database  the database this chat belongs to
     */
    public Chat(Patient patient, Therapist therapist, MongoDatabase database) {
    	
    	MongoCollection<Document> collection = database.getCollection("chats");
    	
    	ObjectId id = null; 
    	
    	boolean duplicateKey;
    	
    	do {
    		
    		duplicateKey = false;
    		
    		String random = new Integer(new Random().nextInt()).toString();
        	byte[] possibleIdAsBytes = (patient.getEmail() + "P" + therapist.getEmail() + "T" + random).getBytes();
        	
        	id = new ObjectId(possibleIdAsBytes);
        	
        	try {
        		collection.insertOne(new Document("_id", id));
        	} catch(MongoWriteException e) {
        		if(e.getCode() == 11000)
        		duplicateKey = true;
        	}
        	
    	} while(duplicateKey);
    	
    	this.id = id;
    	this.database = database;
    	
    	this.patient = patient;
    	this.therapist = therapist;
    	Document chatDoc = collection.find(eq(id)).first();
    	
    	insertIntoCollection();
    	
    }
    
    
    
    
    /**
     * 
     * @return the patient associated with this chat.
     */
    public Patient getPatient() {
        return patient;
    }
    
    /**
     * 
     * @return the therapist associated with this chat.
     */
    public Therapist getTherapist() {
        return therapist;
    }
    
    /**
     * 
     * @return the <code>List</code> of messages that make up the chat.
     */
    public Collection<Message> getMessages() {
        return messages;
    }
    
    /**
     * Retrieves the most recent messages.
     * 
     * @param numberOfMessages the number of messages to return
     * @return                 an array of the recent messages. The size of the 
     *                         array is determined by the parameter 
     *                         <code>numberOfMessages</code>.
     */
    public Message[] getPreviousMessages(int numberOfMessages) throws NoSuchElementException {
        
        Iterator<Message> itr = messages.descendingIterator();
        
        Message[] previousMessages = new Message[numberOfMessages];
        
        for(int i = 0; itr.hasNext() && i < numberOfMessages; i++) {
            
            try {
                previousMessages[i] = itr.next();
            } catch(NoSuchElementException e) {
                throw e;
            }
            
        }
        
        return previousMessages;
        
    }
    
    /**
     * 
     * @param patient the patient to be associated with the chat.
     */
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
    /**
     * 
     * @param therapist the therapist to be associated with the chat.
     */
    public void setTherapist(Therapist therapist) {
        this.therapist = therapist;
    }
    
    /**
     * 
     * @param message the new message to be added to the chat.
     */
    public void addMessage(Message message) {
        messages.add(message);
    }
    
    public void insertIntoCollection() throws MongoWriteException, MongoWriteConcernException, MongoException {
    	
    	MongoCollection<Document> collection = database.getCollection("chats");
    	
    	Document doc = new Document("_id",id)
    			.append("patient_id", patient.getId())
    			.append("therapist_id", therapist.getId())
    			.append("messageIds", null);
    	
    	collection.insertOne(doc);
    	
    	List<ObjectId> messageIds = messages.stream()
    									.map(e -> e.getId())
    									.collect(Collectors.toList());
    	
    	collection.findOneAndUpdate( eq(id), Updates.pushEach("messageIds", messageIds));
    			
    }
    
    public void updateCollection() throws MongoWriteException, MongoWriteConcernException, MongoException {
    	
    	MongoCollection<Document> collection = database.getCollection("chats");
    	
    	List<ObjectId> messageIds = messages.stream()
				.map(e -> e.getId())
				.collect(Collectors.toList());

    	Document doc = new Document("_id", id)
    			.append("patient_id", patient.getId())
    			.append("therapist_id", therapist.getId());
    	
    	try {
    		
    		collection.updateOne(eq(id), doc);
    		collection.findOneAndUpdate( eq(id), Updates.pushEach("message_ids", messageIds));
    		
    	} catch(MongoWriteException e) {
    		throw e;
    	} catch(MongoWriteConcernException e) {
    		throw e;
    	} catch(MongoException e) {
    		throw e;
    	}
    	
    }
	
}