import { test } from 'node:test';
import assert from 'node:assert/strict';
import { assertUrlIsSafe, isPrivateOrReservedIp, SsrfError } from '../src/security/ssrf.js';

test('allows a normal public https URL', async () => {
  const result = await assertUrlIsSafe('https://example.com');
  assert.equal(result.hostname, 'example.com');
  assert.ok(result.resolvedIps.length > 0);
});

test('rejects unsupported protocol', async () => {
  await assert.rejects(() => assertUrlIsSafe('file:///etc/passwd'), SsrfError);
  await assert.rejects(() => assertUrlIsSafe('javascript:alert(1)'), SsrfError);
  await assert.rejects(() => assertUrlIsSafe('data:text/html,hi'), SsrfError);
});

test('rejects malformed URL', async () => {
  await assert.rejects(() => assertUrlIsSafe('not a url'));
});

test('rejects localhost hostnames', async () => {
  await assert.rejects(() => assertUrlIsSafe('http://localhost:3000'), SsrfError);
  await assert.rejects(() => assertUrlIsSafe('http://localhost.localdomain'), SsrfError);
});

test('rejects literal loopback and private IPv4 addresses', async () => {
  await assert.rejects(() => assertUrlIsSafe('http://127.0.0.1'), SsrfError);
  await assert.rejects(() => assertUrlIsSafe('http://127.0.0.1:8080/admin'), SsrfError);
  await assert.rejects(() => assertUrlIsSafe('http://10.0.0.5'), SsrfError);
  await assert.rejects(() => assertUrlIsSafe('http://172.16.5.5'), SsrfError);
  await assert.rejects(() => assertUrlIsSafe('http://192.168.1.1'), SsrfError);
  await assert.rejects(() => assertUrlIsSafe('http://169.254.169.254'), SsrfError); // cloud metadata endpoint
  await assert.rejects(() => assertUrlIsSafe('http://100.64.0.1'), SsrfError);
});

test('rejects literal IPv6 loopback and link-local addresses', async () => {
  await assert.rejects(() => assertUrlIsSafe('http://[::1]'), SsrfError);
  await assert.rejects(() => assertUrlIsSafe('http://[fe80::1]'), SsrfError);
  await assert.rejects(() => assertUrlIsSafe('http://[fc00::1]'), SsrfError);
});

test('isPrivateOrReservedIp classifies ranges correctly', () => {
  assert.equal(isPrivateOrReservedIp('8.8.8.8'), false);
  assert.equal(isPrivateOrReservedIp('1.1.1.1'), false);
  assert.equal(isPrivateOrReservedIp('127.0.0.1'), true);
  assert.equal(isPrivateOrReservedIp('10.1.2.3'), true);
  assert.equal(isPrivateOrReservedIp('172.31.255.255'), true);
  assert.equal(isPrivateOrReservedIp('172.32.0.1'), false); // just outside 172.16.0.0/12
  assert.equal(isPrivateOrReservedIp('192.168.0.1'), true);
  assert.equal(isPrivateOrReservedIp('169.254.1.1'), true);
  assert.equal(isPrivateOrReservedIp('::1'), true);
  assert.equal(isPrivateOrReservedIp('fe80::abcd'), true);
  assert.equal(isPrivateOrReservedIp('2001:4860:4860::8888'), false); // public (Google DNS)
});
