package ua.mykola.questservice.entity;

import commons.dto.QuestCode;
import jakarta.persistence.*;


@Entity
@Table(name = "quests")
public class QuestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private QuestCode code;

    private String name;
    private String description;
    private String address;
    private String phone;

    public QuestEntity() {
    }

    public QuestEntity(Long id, QuestCode code, String name, String description, String address, String phone) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuestCode getCode() {
        return code;
    }

    public void setCode(QuestCode code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
