package com.tujuhsembilan.example.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tujuhsembilan.example.model.Note;

public interface NoteRepo extends JpaRepository<Note, UUID> {
    List<Note> findByUsername(String username);
}
