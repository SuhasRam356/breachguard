package com.breachguard.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** DPDP Act erasure/grievance letter generator — exact port of Python legal.py. */
@Service
public class LegalService {

    public String buildErasureLetter(String userName, String userEmail, String userPhone,
                                     String companyName, String companyEmail,
                                     String breachName, String details) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        String phoneLine = (userPhone != null && !userPhone.isBlank()) ? "    Phone: " + userPhone + "\n" : "";
        String breachRef = (breachName != null && !breachName.isBlank()) ? breachName : "not specified";
        String company = (companyName != null && !companyName.isBlank()) ? companyName : "[Company Name]";
        String compEmail = (companyEmail != null && !companyEmail.isBlank()) ? companyEmail : "[grievance email]";
        String detailsLine = (details != null && !details.isBlank()) ? "\nAdditional details: " + details + "\n" : "";

        return "Subject: Request for Erasure of Personal Data & Grievance —\n"
                + "Ref: Data Breach (" + breachRef + ")\n\n"
                + "Date: " + today + "\n\n"
                + "To,\n"
                + "The Data Protection Officer / Grievance Officer,\n"
                + company + "\n"
                + "Email: " + compEmail + "\n\n"
                + "Dear Sir/Madam,\n\n"
                + "I am an Indian resident and a user/customer of " + company + ".\n"
                + "I have learned that my personal data was exposed in the data breach\n"
                + "known as \"" + breachRef + "\".\n\n"
                + "My details associated with your service:\n"
                + "    Name : " + userName + "\n"
                + "    Email: " + userEmail + "\n"
                + phoneLine + "\n"
                + "Under the Digital Personal Data Protection Act, 2023:\n\n"
                + "1. Section 12 grants me the RIGHT TO ACCESS, CORRECTION and ERASURE of my\n"
                + "   personal data. I hereby request the ERASURE (deletion) of all personal\n"
                + "   data held about me, including any data shared with third parties, subject\n"
                + "   only to retention required by law.\n\n"
                + "2. I request CONFIRMATION in writing of:\n"
                + "   (a) what categories of personal data of mine you process;\n"
                + "   (b) confirmation that my data has been erased from all systems and\n"
                + "       third-party processors;\n"
                + "   (c) details of how the breach occurred and the data of mine affected.\n"
                + detailsLine + "\n"
                + "3. As per Section 13, if I do not receive a satisfactory response within\n"
                + "   30 days, I intend to escalate this complaint to the Data Protection\n"
                + "   Board of India and/or CERT-In (incident@cert-in.org.in).\n\n"
                + "I request an acknowledgement of this request within 72 hours.\n\n"
                + "Thank you,\n\n"
                + "Yours faithfully,\n"
                + userName + "\n"
                + userEmail + "\n"
                + (userPhone != null ? userPhone : "") + "\n";
    }
}
