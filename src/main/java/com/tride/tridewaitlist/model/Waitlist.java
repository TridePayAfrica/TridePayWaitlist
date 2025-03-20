    package com.tride.tridewaitlist.model;

    import com.fasterxml.jackson.annotation.JsonFormat;
    import jakarta.persistence.Entity;
    import jakarta.persistence.GeneratedValue;
    import jakarta.persistence.GenerationType;
    import jakarta.persistence.Id;
    import lombok.Data;

    import java.time.LocalDateTime;

    @Entity
    @Data
    public class Waitlist {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String fullName;
        private String email;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime joinDate;

        public LocalDateTime getJoinDate() {
            return joinDate;
        }

        public void setJoinDate(LocalDateTime joinDate) {
            this.joinDate = joinDate;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }