package site.heeseong.chatting_server.manager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import site.heeseong.chatting_server.model.Event;
import site.heeseong.chatting_server.model.Users;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@Data
public class ChattingUser {
	private Users user;
	private int programIdx;
	private ArrayBlockingQueue<Event> messageQueue;
	private long latestMessageTime;
	private long DEFAULT_MESSAGE_TIMEOUT = 60 * 1000 * 2;	 // 2 minutes
	private long userTimeout = DEFAULT_MESSAGE_TIMEOUT;
	
	public ChattingUser() {
	}

	public ChattingUser(Users userInfo) {
		user = userInfo;
		messageQueue = new ArrayBlockingQueue<Event>(10);
		latestMessageTime = System.currentTimeMillis();
	}

	@JsonIgnore
	public Users getUser() {
		return user;
	}
	public void setUser(Users user) {
		this.user = user;
	}

	public long getUserIdx() {
		return user.getUserIdx();
	}

	public long getInternalIdx() {
		return user.getInternalIdx();
	}

	public String getUserId() {
		return user.getUserId();
	}

	public String getUserName() {
		return user.getUserName();
	}

	public void postMessage(Event event) {
		if (messageQueue != null) {
			try {
				messageQueue.add(event);
			} catch (Exception e) {
				decreaseUserTimeOut();
				e.printStackTrace();
			}
		}
	}
	
	private void decreaseUserTimeOut() {
		userTimeout = userTimeout / 2;
		if (userTimeout < 60000) {
			userTimeout = 60000;
		}
	}
	
	public boolean checkTimeout() {
		if (latestMessageTime != 0) {
			if ((System.currentTimeMillis() - latestMessageTime) > userTimeout) {
				return true;
			}
		}
		return false;
	}
	
	private void setLatestTime() {
		latestMessageTime = System.currentTimeMillis();
	}
	
	@JsonIgnore
	public ArrayList<Event> getEvents() throws Exception {
		setLatestTime();
		ArrayList<Event> events = new ArrayList<Event>();
		if (messageQueue != null) {
			try {
				Event event = messageQueue.poll(5000, TimeUnit.MILLISECONDS);
				if (event != null && messageQueue != null) {
					events.add(event);
					if (messageQueue.size() != 0) {
						for (int i = 0; i < messageQueue.size(); i++) {
							events.add(messageQueue.take());
						}
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return events;
	}
	
	public void removeAll() {
		if (messageQueue != null) {
			messageQueue.clear();
			messageQueue = null;
		}
	}

	public boolean isAdmin() {
		return user.isAdmin();
	}

}
