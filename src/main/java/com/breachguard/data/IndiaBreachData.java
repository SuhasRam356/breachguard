package com.breachguard.data;

import java.util.List;
import java.util.Map;

/** Curated feed of major breaches affecting Indian users — port of india_breaches.py. */
public final class IndiaBreachData {

    public static final List<Map<String, String>> INDIA_BREACHES = List.of(
            Map.of("name","MobiKwik (alleged)","year","2021","users","~3.5 million (alleged)","data","Names, phone numbers, email addresses, hashed passwords, KYC details","status","Alleged / company denied","summary","A threat researcher claimed an 8.2 TB database containing user and KYC data of MobiKwik users was for sale on the dark web.","ref","https://haveibeenpwned.com/PwnedWebsites"),
            Map.of("name","BigBasket","year","2020","users","~20 million","data","Email addresses, names, phone numbers, password hashes, PINs, addresses","status","Reported","summary","Data of over 2 crore BigBasket users was reportedly sold on the dark web; the breach was traced to an unsecure cloud storage bucket.","ref","https://haveibeenpwned.com/PwnedWebsites"),
            Map.of("name","Air India","year","2021","users","~4.5 million","data","Names, dates of birth, contact info, passport and ticket details","status","Confirmed","summary","Cyber attack on Air India's passenger service vendor exposed personal data registered between 2011 and 2021.","ref","https://haveibeenpwned.com/PwnedWebsites"),
            Map.of("name","Justdial (alleged)","year","2019","users","~100 million (alleged)","data","Names, phone numbers, email addresses, business details","status","Alleged","summary","Reports claimed a massive Justdial database was left exposed online; the company disputed the scale of exposure.","ref","https://haveibeenpwned.com/PwnedWebsites"),
            Map.of("name","Domino's India","year","2021","users","~18 crore orders (alleged)","data","Names, phone numbers, email addresses, order details, addresses","status","Alleged","summary","A hacker claimed to have 18 crore Domino's India order records; Jubilant FoodWorks said no financial data was involved.","ref","https://haveibeenpwned.com/PwnedWebsites"),
            Map.of("name","SBI / bank phishing campaigns","year","Recurring","users","Ongoing threat","data","OTPs, card details, UPI PINs (via phishing, not breach)","status","Active threat","summary","CERT-In repeatedly warns of SMS/WhatsApp phishing impersonating SBI and other banks to steal OTPs and UPI credentials.","ref","https://www.cert-in.org.in/"),
            Map.of("name","Unacademy","year","2020","users","~22 million (alleged)","data","Email addresses, names, hashed passwords, join dates","status","Alleged","summary","Cyble researchers claimed Unacademy user data was being sold on a dark-web forum; Unacademy said no financial data was exposed.","ref","https://haveibeenpwned.com/PwnedWebsites"),
            Map.of("name","Telegram-linked job scams (India)","year","Recurring","users","Ongoing threat","data","Phone numbers, UPI IDs, OTPs (via social engineering)","status","Active threat","summary","Victims are lured via fake job/part-time tasks; leaked phone numbers and breach data are used to target people convincingly.","ref","https://www.cert-in.org.in/")
    );

    public static final List<String[]> DPDP_RIGHTS = List.of(
            new String[]{"Right to access information (Section 12)", "You can ask a company what personal data they hold about you and how it is processed."},
            new String[]{"Right to correction & erasure (Section 12)", "You can demand correction of inaccurate data or erasure of your data, subject to legal retention rules."},
            new String[]{"Right to grievance redressal (Section 13)", "You can complain to a company's Data Protection Officer / grievance officer; unresolved complaints can go to the Data Protection Board."},
            new String[]{"Breach notification (Section 8(6))", "Organisations must report personal data breaches to the Data Protection Board and affected individuals. Watch for CERT-In advisories."}
    );

    private IndiaBreachData() {}
}
