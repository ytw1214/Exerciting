package com.exerciting.Exerciting.Domain.invitation.entity;

import jakarta.persistence.Entity;

public class Invitation {
    private Long Id;
    private InvitationStatus status;
    private String matchName;
    private String invitationSender;
    private String invitationReceiver;

}
