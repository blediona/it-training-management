package models.mappers;

import models.User;
import models.dto.IRequestDto;
import models.dto.SignupRequestDto;

import java.sql.ResultSet;

public class UserMapper implements Mapper<User> {
    @Override
    public User getFromResultSet(ResultSet res) {
        try {
            return User.getFromResultSet(res);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public User fromDto(IRequestDto dto) {
        return null;
    }

    @Override
    public User fromDto(User obj, IRequestDto dto) {
        return null;
    }

    public User fromSignUpDto(SignupRequestDto signUpDto, String salt, String saltedHash) {
        return new User(
                0,
                signUpDto.getFullName(),
                signUpDto.getEmail(),
                signUpDto.getUsername(),
                salt,
                saltedHash,
                "STAFF"
        );
    }
}
