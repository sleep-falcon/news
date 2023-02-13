package com.imooc.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckNameValidate.class)
public @interface CheckName  {
    String message() default "用户名不正确";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
