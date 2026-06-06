import { Database } from '@nozbe/watermelondb';
import SQLiteAdapter from '@nozbe/watermelondb/adapters/sqlite';
import { dbSchema } from './schema';
import SyncMeta from './SyncMeta';

const adapter = new SQLiteAdapter({
  schema: dbSchema,
  jsi: true,
  onSetUpError: error => {
    console.error('WatermelonDB adapter setup error', error);
  },
});

export const database = new Database({
  adapter,
  modelClasses: [SyncMeta],
});

export { SyncMeta };
