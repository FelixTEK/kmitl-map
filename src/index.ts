import { Elysia } from 'elysia';
import { cors } from '@elysiajs/cors';
import postgres from 'postgres';

const sql = postgres(process.env.DATABASE_URL!);

const port = Number(process.env.PORT) || 3000;

const app = new Elysia()
  .use(cors())
  .get('/api/locations', async () => {
    const locations = await sql`SELECT * FROM locations`;
    return locations;
  })
  .post('/api/reports', async ({ body }: { body: { description: string; category?: string } }) => {
    const { description, category = 'other' } = body;
    await sql`INSERT INTO issue_reports (description, category) VALUES (${description}, ${category})`;
    return { success: true, message: 'แจ้งปัญหาเรียบร้อย!' };
  })
  .listen(port);

console.log(`🦊 Elysia API วิ่งอยู่บนพอร์ต ${app.server?.port}`);