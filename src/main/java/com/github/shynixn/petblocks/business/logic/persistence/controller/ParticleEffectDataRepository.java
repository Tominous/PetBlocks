package com.github.shynixn.petblocks.business.logic.persistence.controller;

import com.github.shynixn.petblocks.api.persistence.controller.ParticleEffectMetaController;
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta;
import com.github.shynixn.petblocks.business.logic.persistence.entity.ParticleEffectData;
import com.github.shynixn.petblocks.lib.DataBaseRepository;
import com.github.shynixn.petblocks.lib.ExtensionHikariConnectionContext;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ParticleEffectDataRepository extends DataBaseRepository<ParticleEffectMeta> implements ParticleEffectMetaController {

    private ExtensionHikariConnectionContext dbContext;

    public ParticleEffectDataRepository(ExtensionHikariConnectionContext connectionContext) {
        super();
        this.dbContext = connectionContext;
    }

    /**
     * Creates a new particleEffectMeta
     * @return meta
     */
    @Override
    public ParticleEffectMeta create() {
        return new ParticleEffectData();
    }

    /**
     * Returns the item of the given id
     *
     * @param id id
     * @return item
     */
    @Override
    public ParticleEffectMeta getById(long id) {
        try (Connection connection = this.dbContext.getConnection()) {
            try (PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("particle/selectbyid", connection,
                    id)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    return this.from(resultSet);
                }
            }
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
        return null;
    }

    /**
     * Checks if the item has got an valid databaseId
     *
     * @param item item
     * @return hasGivenId
     */
    @Override
    public boolean hasId(ParticleEffectMeta item) {
        return item.getId() != 0;
    }

    /**
     * Selects all items from the database into the list
     *
     * @return listOfItems
     */
    @Override
    public List<ParticleEffectMeta> select() {
        final List<ParticleEffectMeta> items = new ArrayList<>();
        try (Connection connection = this.dbContext.getConnection()) {
            try (PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("particle/selectall", connection)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        final ParticleEffectMeta data = this.from(resultSet);
                        items.add(data);
                    }
                }
            }
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
        return  items;
    }

    /**
     * Updates the item inside of the database
     *
     * @param item item
     */
    @Override
    public void update(ParticleEffectMeta item) {
        try (Connection connection = this.dbContext.getConnection()) {
            String materialName = null;
            if(item.getMaterial() != null)
                materialName = item.getMaterial().name();
            this.dbContext.executeStoredUpdate("particle/update", connection,
                    item.getEffectName(),
                    item.getAmount(),
                    item.getSpeed(),
                    item.getX(),
                    item.getY(),
                    item.getZ(),
                    materialName,
                    (int)item.getData(),
            item.getId());
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
    }

    /**
     * Deletes the item from the database
     *
     * @param item item
     */
    @Override
    public void delete(ParticleEffectMeta item) {
        try (Connection connection = this.dbContext.getConnection()) {
            this.dbContext.executeStoredUpdate("particle/delete", connection,
                    item.getId());
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
    }

    /**
     * Inserts the item into the database and sets the id
     *
     * @param item item
     */
    @Override
    public void insert(ParticleEffectMeta item) {
        try (Connection connection = this.dbContext.getConnection()) {
            String materialName = null;
            if(item.getMaterial() != null)
                materialName = item.getMaterial().name();
            final long id = this.dbContext.executeStoredInsert("particle/insert", connection,
                    item.getEffectName(),
                    item.getAmount(),
                    item.getSpeed(),
                    item.getX(),
                    item.getY(),
                    item.getZ(),
                    materialName,
                    item.getData());
            ((ParticleEffectData)item).setId(id);
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
    }

    /**
     * Returns the amount of items in the repository
     */
    @Override
    public int size() {
        try (Connection connection = this.dbContext.getConnection()) {
            try (PreparedStatement preparedStatement = this.dbContext.executeStoredQuery("particle/count", connection)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSet.next();
                    return resultSet.getInt(1);
                }
            }
        } catch (final SQLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Database error occurred.", e);
        }
        return 0;
    }

    /**
     * Generates the entity from the given resultSet
     *
     * @param resultSet resultSet
     * @return entity
     */
    @Override
    public ParticleEffectMeta from(ResultSet resultSet) throws SQLException{
        final ParticleEffectData particleEffectData = new ParticleEffectData();
        particleEffectData.setId(resultSet.getLong("id"));
        particleEffectData.setEffectName(resultSet.getString("name"));
        particleEffectData.setAmount(resultSet.getInt("amount"));
        particleEffectData.setSpeed(resultSet.getDouble("speed"));
        particleEffectData.setX(resultSet.getDouble("x"));
        particleEffectData.setY(resultSet.getDouble("y"));
        particleEffectData.setZ(resultSet.getDouble("z"));
        particleEffectData.setMaterial(Material.getMaterial(resultSet.getString("material")));
        particleEffectData.setData((byte) resultSet.getInt("data"));
        return particleEffectData;
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     * <p>
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     * <p>
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     * <p>
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     * <p>
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        this.dbContext = null;
    }
}