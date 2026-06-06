import { appSchema, tableSchema } from '@nozbe/watermelondb';

export const dbSchema = appSchema({
  version: 1,
  tables: [
    tableSchema({
      name: 'sync_meta',
      columns: [
        { name: 'key', type: 'string', isIndexed: true },
        { name: 'value', type: 'string' },
        { name: 'updated_at', type: 'number' },
      ],
    }),
  ],
});
