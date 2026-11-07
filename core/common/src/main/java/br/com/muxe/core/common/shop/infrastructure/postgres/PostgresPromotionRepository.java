package br.com.muxe.core.common.shop.infrastructure.postgres;

import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import br.com.muxe.core.common.shop.domain.Promotion;
import br.com.muxe.core.common.shop.domain.PromotionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class PostgresPromotionRepository implements PromotionRepository {

    private static final int THE_PROMOTION = 1;

    private static final Table PROMOTION = Table.of("core", "shop_promotion");
    private static final Column<Integer> ID = PROMOTION.column("id", SqlType.SMALLINT);
    private static final Column<String> PRODUCT_ID = PROMOTION.column("product_id", SqlType.TEXT);
    private static final Column<List<String>> LINES = PROMOTION.column("lines", SqlType.TEXT_ARRAY);
    private static final Column<Instant> CHANGED_AT = PROMOTION.column("changed_at", SqlType.TIMESTAMPTZ);

    private static final Select<Promotion> FIND = Sql.select(PRODUCT_ID, LINES).from(PROMOTION)
        .where(ID.eq())
        .map(row -> new Promotion(row.get(PRODUCT_ID), row.get(LINES)))
        .named("shop.find_promotion");

    private static final Write<Void> SAVE = Sql.<Void>insertInto(PROMOTION)
        .value(ID)
        .value(PRODUCT_ID)
        .value(LINES)
        .value(CHANGED_AT)
        .onConflict(ID).doUpdate(PRODUCT_ID, LINES, CHANGED_AT)
        .named("shop.save_promotion");

    @Override
    public Optional<Promotion> find() {
        return FIND.first(ID.is(THE_PROMOTION));
    }

    @Override
    public void save(Promotion promotion, Instant changedAt) {
        SAVE.execute(ID.is(THE_PROMOTION), PRODUCT_ID.is(promotion.productId()), LINES.is(promotion.lines()), CHANGED_AT.is(changedAt));
    }
}
