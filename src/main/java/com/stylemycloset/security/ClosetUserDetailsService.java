package com.stylemycloset.security;

import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.exception.UserNotFoundException;
import com.stylemycloset.user.mapper.UserMapper;
import com.stylemycloset.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClosetUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional(readOnly = true)
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    log.info(email);

    User user = userRepository.findByEmail(email)
        .orElseThrow(UserNotFoundException::new);

    if (user.isLocked()) {
      throw new LockedException("잠긴 계정입니다.");
    }

    return new ClosetUserDetails(userMapper.UsertoUserDto(user), user.getPassword());
  }
}
