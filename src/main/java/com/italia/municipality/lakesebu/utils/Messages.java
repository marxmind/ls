package com.italia.municipality.lakesebu.utils;

import java.util.Calendar;
import java.util.Random;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Messages {

	private String message;
	private String[] mornings;
	
	public static String greetings(String user) {
		String greetings = "";
		Calendar c = Calendar.getInstance();
		int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
		if(timeOfDay >= 0 && timeOfDay < 12){
			greetings = Messages.getMorningMessage(user);
		}else if(timeOfDay >= 12 && timeOfDay < 16){
			greetings = "Good afternoon";
		}else if(timeOfDay >= 16 && timeOfDay < 21){
			greetings = "Good evening";
		}else if(timeOfDay >= 21 && timeOfDay < 24){
			greetings = "Good night";
		}
		
		return greetings;
	}
	
	public static String getMorningMessage(String user) {
		 
		String[] msg = {
				"May you have a positive day and find solutions to all your problems. " + user,
				"May the brightness of the sun shines upon you like blessings. Good morning " + user + "!",
				"Wishing you a lovely good morning "+ user +"! It�s a beautiful day since you are part of it.",
				"I love watching every morning in my life as they always give me one more chance to spend one more day with you. Good morning my friend " + user,
				"I hope your morning is filled with love, happiness, peace, and harmony. Good morning, " + user+"!",
				"A morning spent without a lovely friend like "+ user +" is completely wasted. Wake up, my dear friend. We�re definitely going to have an amazing day! Good morning!",
				"You know this morning is so cozy and beautiful that I really don�t want you to miss it at any cost. So, get up a dear friend "+ user +", and get down to business.",
				"Birds are singing a sweet song and a gentle breeze is blowing through the trees, what a perfect combination and morning to get you up. Good morning " + user + "!",
				"Stop letting the noises inside your head, overpower the happiness that lies outside. Good morning " + user+"!",
				"The only contagious thing that everyone in the office looks forward to catch, is your infectious smile. Good morning " + user + "!",
				user + ", You are the Anti-Virus of my work life. You guard me against malicious attacks, heal me when I break down and always protect with your unconditional support. Good morning.",
				"May the rays of the morning sun give you the energy to squeeze out every last drop of your talent and ability to rise above the rest. Good morning. " + user + "!",
				"Every sunrise is life�s way of saying that no matter how dark a past you�ve had, there will always be a new beginning out there� as long as you�re willing to persevere, take a step forward and do the right thing. Good morning. " + user + "!",
				"If you can conquer the snooze, you can conquer anything. Good morning " + user + "!",
				"Monday morning blues, become vibrant Monday morning hues, when I have colleagues like you. Good morning " + user + ".",
				"Get over your hangover or your boss will get over you very soon. Good morning " + user + ".",
				user + ", Learn and work as if it is your first day in the office, but behave as if it is your last � that�s the secret to carving out a successful career. Good morning.",
				"Hi " + user + ", Facing your failures and overcoming your fears is the only way to succeed. Good morning.",
				"Hi dear friend " + user + ", Everyone has bad days. Don�t let the baggage ruin your good ones. Good morning.",
				"Great you are here my friend " + user + ", That lucrative promotion, amazing perks, big assignment, money-spinning deal and massively rewarding project will be all yours, on one condition � energize, smile and give it everything you�ve got. Good morning.",
				user + ", Whether it is Monday, Wednesday or Friday, as long as I have colleagues like you, it will always be AwesomeDay. Good morning.",
				"Hi dearest friend " + user + ", The office would be gloomy and blue, if it weren�t for fun colleagues like you. Good morning.",
				user + ", Just like how hard work is the foundation for success, a productive morning is the foundation for a great day at work. Good morning.",
				user + ", Wishing you all the best, for being the best that you can be today. Good morning.",
				"Failures, setbacks, weaknesses � none of these can stop you as long as your desire to succeed overpowers the negativity in your mind. Just tell yourself one thing when you wake up in the morning � Be Positive " + user + " :)",
				user + ", Don�t run away from your mistakes. Fix them, learn from them and understand what you need to do so that you never repeat them. This is the only formula of success. Good morning.",
				user +", In life, don�t be negative. It will not only stop you from getting where you want to be, but also ruin what you already have. Good morning.",
				user + ", Targets for the day may be high, but your potential is much higher. Good morning.",
				"May you rise up the corporate ladder of success, just like how the run rises in the morning. Good morning " + user + "!",
				"Early morning starts are the best way to get work done before the chaos and background noise of the rest of the day takes over. Good morning " + user + ".",
				"Did you know that " + user + ", The only difference between people who are successful and those who aren�t, is that successful people face their challenges while others run away from them. Good morning.",
				"Hi" + user + ", Every morning, you get an opportunity to write a new chapter in the destiny of your career. Good morning.",
				"Most successful people rely on early mornings to think, plan, strategize and get work done. Are you one of them? Good morning " + user + ".",
				"Every morning is an opportunity to redeem yourself of the mistakes of your past and push yourself towards a successful future. Good morning " + user + "."
		
		};
		int max = msg.length-1, min =0;
		Random rand = new Random();
		int index = rand.nextInt((max-min)+1) + min; 
		
		
		return msg[index];
	}
	
}
