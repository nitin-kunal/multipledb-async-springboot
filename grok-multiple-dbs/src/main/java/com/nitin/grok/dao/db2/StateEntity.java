package com.nitin.grok.dao.db2;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "state",schema = "APPUSER")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateEntity {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}