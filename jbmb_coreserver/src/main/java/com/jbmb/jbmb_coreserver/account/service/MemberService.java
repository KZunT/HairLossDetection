package com.jbmb.jbmb_coreserver.account.service;

import com.jbmb.jbmb_coreserver.account.dto.InformationResponse;
import com.jbmb.jbmb_coreserver.account.dto.LoginResponse;
import com.jbmb.jbmb_coreserver.account.dto.LogoutResponse;
import com.jbmb.jbmb_coreserver.account.domain.Member;
import com.jbmb.jbmb_coreserver.account.dto.SignupResponse;
import com.jbmb.jbmb_coreserver.account.jwt.JwtTokenProvider;
import com.jbmb.jbmb_coreserver.account.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private final SignupResponse signup = new SignupResponse();
    private final LoginResponse login = new LoginResponse();
    private final LogoutResponse logout = new LogoutResponse();
    private final InformationResponse information = new InformationResponse();

    /**
     * 회원가입 버튼 클릭 시
     * resultCode 0:성공 , 1:ID 중복, 2:실패
     * @param Member
     * @return Signup
     */
    public SignupResponse joinService(Member user){
        if(memberRepository.findById(user.getId()).isPresent()) return signup
                .builder()
                .resultCode(1)
                .build(); // 중복 체크
        try {
            memberRepository.save(Member.builder()
                    .userNum(user.getUserNum())
                    .id(user.getId())
                    .password(passwordEncoder.encode(user.getPassword()))
                    .name(user.getName())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .sex(user.getSex())
                    .age(user.getAge())
                    .hairType(user.getHairType())
                    .roles(Collections.singletonList("ROLE_USER")) // 최초 가입시 USER 로 설정
                    .build());
            return signup
                    .builder()
                    .resultCode(0)
                    .build();
        } catch (Exception e){
            return signup
                    .builder()
                    .resultCode(2)
                    .build();
        }
    }

    /**
     * 로그인 버튼 클릭 시
     * { resultCode 0:성공 , 1:존재하지 않는 ID, 2:비밀번호 오류
     *   jwt : jwt }
     * @param Member
     * @return Login
     */
    public LoginResponse loginService(Member user){
        Optional<Member> member=memberRepository.findById(user.getId());
        if (!member.isPresent()) return login
                                        .builder()
                                        .resultCode(1)
                                        .build(); // 가입되지 않은 ID
        if (!passwordEncoder.matches(user.getPassword(), member.get().getPassword())) {
            return login
                    .builder()
                    .resultCode(2)
                    .build(); // 잘못된 비밀번호
        }
        return login.builder()
                .resultCode(0)
                .jwt(jwtTokenProvider.createToken(member.get().getId(), member.get().getRoles()))
                .build();
    }

    /**
     * 로그아웃 버튼 클릭 시
     * resultCode 0:성공 , 1:ID 이미 로그아웃, 2:Invalid token
     * @param HttpServletRequest
     * @return Logout
     */
    public LogoutResponse logoutService(HttpServletRequest req){
        String token = jwtTokenProvider.resolveToken(req);
        Integer re=jwtTokenProvider.checkAlreadyLogout(token);
        if (re==0) {
            Date expirationDate = jwtTokenProvider.getExpirationDate(token);
            redisTemplate.opsForValue().set(
                    token, "l",
                    expirationDate.getTime() - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS );
            log.info("redis value : "+redisTemplate.opsForValue().get(token));
        }
        return logout
                .builder()
                .resultCode(re)
                .build();
    }

    /**
     * 회원정보 가져오기
     * resultCode 0:성공 , 1:실패
     * 성공 시 id, name, phoneNumber, sex, age, hairType 리턴
     * @param ServeletRequest
     * @return Logout
     */
    public InformationResponse getInfoService(ServletRequest req){
        Optional<Member> member;
        try {
            String id = jwtTokenProvider.getUserPk(jwtTokenProvider.resolveToken((HttpServletRequest) req));
            member=memberRepository.findById(id);
        }catch (Exception e){
            return information.builder().resultCode(1).build();
        }
        return information.builder()
                .resultCode(0)
                .id(member.get().getId())
                .name(member.get().getName())
                .phoneNumber(member.get().getPhone())
                .sex(member.get().getSex())
                .age(member.get().getAge())
                .hairType(member.get().getHairType())
                .build();
    }
}