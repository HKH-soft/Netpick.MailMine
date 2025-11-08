package ir.netpick.mailmine.auth.dto;

public record UserUpdateRequest(
        String name,
        String preference,
        String description
){
}
