package it.unical.ea.eventra.data.repository;
import it.unical.ea.eventra.data.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
