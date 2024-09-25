package com.vendor.util;

import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.vendor.model.ProductOrder;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;


@Component
public class CommonUtil {

	@Autowired
	private JavaMailSender mailSender;

	public Boolean sendMail(String url, String reciepentEmail,String userName) throws UnsupportedEncodingException, MessagingException {

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom("rakeshibm909@gmail.com", "Cart Wala India Pvt Ltd.");
		helper.setTo(reciepentEmail);

		String content = "<p>Hello,"+userName+"</p>" + "<p>You have requested to reset your password.</p>"
				+ "<p>Click the link below to change your password:</p>" + "<p><a href=\"" + url
				+ "\">Change my password</a></p>";
		helper.setSubject("Password Reset (Cart Wala India Pvt Ltd.)");
		helper.setText(content, true);
		mailSender.send(message);
		return true;
	}

	public static String generateUrl(HttpServletRequest request) {

		String siteUrl = request.getRequestURL().toString();

		return siteUrl.replace(request.getServletPath(), "");
	}
	
	public Boolean sendMailForProductOrder(ProductOrder order, String status) throws Exception {
	    
	    // Load the email template
	    Path templatePath = Paths.get(new ClassPathResource("templates/email-template.html").getURI());
	    String msg = new String(Files.readAllBytes(templatePath));

	    // Replace placeholders with actual values
	    msg = msg.replace("[[name]]", order.getOrderAddress().getFirstName());
	    msg = msg.replace("[[orderStatus]]", status);
	    msg = msg.replace("[[productName]]", order.getProduct().getTitle());
	    msg = msg.replace("[[category]]", order.getProduct().getCategory());
	    msg = msg.replace("[[quantity]]", order.getQuantity().toString());
		//order amount
	    msg = msg.replace("[[price]]", order.getPrice().toString());
	    msg = msg.replace("[[paymentType]]", order.getPaymentType());

	    // Prepare the email
	    MimeMessage message = mailSender.createMimeMessage();
	    MimeMessageHelper helper = new MimeMessageHelper(message, true);

	    helper.setFrom("rakeshibm909@gmail.com", "Cartwala.shop");
	    helper.setTo(order.getOrderAddress().getEmail());
	    helper.setSubject("Product Order: " + status);
	    helper.setText(msg, true);  // Enable HTML content

	    // Send the email
	    mailSender.send(message);

	    return true;
	}
}
