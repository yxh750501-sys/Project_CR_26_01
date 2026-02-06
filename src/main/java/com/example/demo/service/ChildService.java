package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.repository.ChildMapper;
import com.example.demo.vo.Child;

@Service
public class ChildService {

    private final ChildMapper childMapper;

    public ChildService(ChildMapper childMapper) {
        this.childMapper = childMapper;
    }

    public List<Child> getChildren(long userId) {
        return childMapper.findAllByUserId(userId);
    }

    public Child getChild(long id, long userId) {
        return childMapper.findByIdAndUserId(id, userId);
    }

    public void write(long userId, String name, LocalDate birthDate, String gender, String note) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("아이 이름을 입력하세요.");

        Child c = new Child();
        c.setUserId(userId);
        c.setName(name.trim());
        c.setBirthDate(birthDate);
        c.setGender((gender == null || gender.isBlank()) ? "U" : gender);
        c.setNote(note);

        childMapper.insert(c);
    }

    public void modify(long id, long userId, String name, LocalDate birthDate, String gender, String note) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("아이 이름을 입력하세요.");

        Child c = new Child();
        c.setId(id);
        c.setUserId(userId);
        c.setName(name.trim());
        c.setBirthDate(birthDate);
        c.setGender((gender == null || gender.isBlank()) ? "U" : gender);
        c.setNote(note);

        int updated = childMapper.update(c);
        if (updated == 0) throw new IllegalArgumentException("수정 불가(존재X 또는 권한X).");
    }

    public void delete(long id, long userId) {
        int deleted = childMapper.deleteByIdAndUserId(id, userId);
        if (deleted == 0) throw new IllegalArgumentException("삭제 불가(존재X 또는 권한X).");
    }
}
