import { dbSchema } from '../src/db/schema';

describe('WatermelonDB schema', () => {
  it('declares the sync_meta table at schema version 1', () => {
    expect(dbSchema.version).toBe(1);
    const syncMeta = dbSchema.tables.sync_meta;
    expect(syncMeta).toBeDefined();
    expect(syncMeta.name).toBe('sync_meta');
    const columnNames = Object.keys(syncMeta.columns);
    expect(columnNames).toEqual(expect.arrayContaining(['key', 'value', 'updated_at']));
  });

  it('indexes the key column for fast lookup', () => {
    const keyColumn = dbSchema.tables.sync_meta.columns.key;
    expect(keyColumn.isIndexed).toBe(true);
  });
});
