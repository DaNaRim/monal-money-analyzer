package com.danarim.monal.user.web.validator;

import com.danarim.monal.exceptions.InternalServerException;
import com.danarim.monal.user.web.dto.PasswordEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    private static final Log logger = LogFactory.getLog(PasswordMatchesValidator.class);

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        try {
            PasswordEntity passwordEntity = (PasswordEntity) obj;
            return StringUtils.equals(passwordEntity.password(), passwordEntity.matchingPassword());
        } catch (ClassCastException e) {
            logger.error("obj is not a valid PasswordEntity type", e);
            throw new InternalServerException("obj is not a valid PasswordEntity type", e);
        }
    }
}
