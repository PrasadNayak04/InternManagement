package com.robosoft.internmanagement.service.jwtSecurity;

import com.robosoft.internmanagement.modelAttributes.Member;
import com.robosoft.internmanagement.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private MemberService memberService;

    @Override
    public UserDetails loadUserByUsername(String memberEmail) throws UsernameNotFoundException {
        try {
            Member member = Optional.of(memberService.getMemberByEmail(memberEmail)).orElse(null);

            Set<GrantedAuthority> authorities = Arrays.stream(member.getRole().split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());

            return new org.springframework.security.core.userdetails.User(member.getEmailId(), member.getPassword(),
                    authorities);
        }
        catch (Exception e){
            return null;
        }
    }



    public String greet(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return "Hello " + userDetails.getUsername();
    }

}
