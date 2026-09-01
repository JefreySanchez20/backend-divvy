package com.divvy.gastos.infrastructure.web.dto;

import com.divvy.gastos.domain.Gasto;
import com.divvy.gastos.domain.TipoDivision;

public final class GastoDtoMapper {

    private GastoDtoMapper() {
    }

    public static GastoResponse toResponse(Gasto gasto) {
        DivisionResponse division = new DivisionResponse(mapTipoADto(gasto.division().tipo()), gasto.division().detalle());
        return new GastoResponse(
                gasto.id(),
                gasto.descripcion(),
                gasto.monto().monto(),
                gasto.monto().moneda(),
                gasto.pagadoPor(),
                gasto.fecha(),
                gasto.categoria(),
                division
        );
    }

    public static TipoDivision mapTipoADominio(TipoDivisionDto tipo) {
        return switch (tipo) {
            case EQUAL -> TipoDivision.IGUAL;
            case PERCENTAGE -> TipoDivision.PORCENTAJE;
            case FIXED_AMOUNT -> TipoDivision.MONTO_FIJO;
        };
    }

    private static String mapTipoADto(TipoDivision tipo) {
        return switch (tipo) {
            case IGUAL -> "EQUAL";
            case PORCENTAJE -> "PERCENTAGE";
            case MONTO_FIJO -> "FIXED_AMOUNT";
        };
    }
}
