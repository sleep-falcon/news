package com.imooc.validate;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckNameValidate  implements ConstraintValidator<CheckName, String> {
    @Override
    public boolean isValid(String name, ConstraintValidatorContext constraintValidatorContext) {
        if(StringUtils.isNotBlank(name)&&name.length()>=3){
            return true;
        }else {
            return false;
        }
    }
}
