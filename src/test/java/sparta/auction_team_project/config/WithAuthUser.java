package sparta.auction_team_project.config;


import org.springframework.security.test.context.support.WithSecurityContext;
import sparta.auction_team_project.domain.user.enums.UserRole;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory= WithAuthUserSecurityContextFactory.class)
public @interface WithAuthUser {
    long userId() default 1L;
    String email() default "test@test.kr";
    UserRole userRole() default UserRole.ROLE_USER;
}

