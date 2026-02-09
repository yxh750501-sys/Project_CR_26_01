package com.example.demo.service;

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

	public List<Child> getChildrenByUserId(long userId) {
		return childMapper.getChildrenByUserId(userId);
	}

	public Child getChildByIdAndUserId(long id, long userId) {
		return childMapper.getChildByIdAndUserId(id, userId);
	}

	public boolean writeChild(long userId, String name, String birthDate, String gender, String note) {
		return childMapper.writeChild(userId, name, birthDate, gender, note) > 0;
	}

	public boolean modifyChild(long id, long userId, String name, String birthDate, String gender, String note) {
		return childMapper.modifyChild(id, userId, name, birthDate, gender, note) > 0;
	}

	public boolean deleteChild(long id, long userId) {
		return childMapper.deleteChild(id, userId) > 0;
	}
}
