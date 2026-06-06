import { Model } from '@nozbe/watermelondb';
import { field, date } from '@nozbe/watermelondb/decorators';

export default class SyncMeta extends Model {
  static table = 'sync_meta';

  @field('key') key!: string;
  @field('value') value!: string;
  @date('updated_at') updatedAt!: Date;
}
