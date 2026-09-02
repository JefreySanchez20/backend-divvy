package com.divvy.liquidaciones.infrastructure.events;

import com.divvy.shared.domain.events.GastoRegistradoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class GastoRegistradoListener {

    private static final Logger log = LoggerFactory.getLogger(GastoRegistradoListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGastoRegistrado(GastoRegistradoEvent event) {
        log.info(
                "Gasto {} registrado en el grupo {}: el balance del grupo cambió, la próxima consulta de liquidación lo recalculará",
                event.gastoId(), event.grupoId()
        );
    }
}
