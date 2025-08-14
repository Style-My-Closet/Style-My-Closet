package com.stylemycloset.user.util;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

  private final JavaMailSender mailSender;

  public void sendTempPassword(String email, String tempPassword) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(email);
    message.setSubject("[옷장을 부탁해] 임시 비밀번호 안내");
    message.setText(
        "회원님의 임시 비밀번호는 다음과 같습니다: " + tempPassword + "유효 시간은 3분이므로 접속 후 비밀번호 변경을 부탁드립니다.");
    mailSender.send(message);
  }

}
