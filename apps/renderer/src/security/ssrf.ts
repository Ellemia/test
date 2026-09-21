import { promises as dns } from 'node:dns';
import { ERROR_CODES } from '@webshot/shared';

export class SsrfError extends Error {
  code: string;
  constructor(code: string, message: string) {
    super(message);
    this.name = 'SsrfError';
    this.code = code;
  }
}

const BLOCKED_HOSTNAMES = new Set([
  'localhost',
  'localhost.localdomain',
  'ip6-localhost',
  'ip6-loopback',
]);

function ipv4ToLong(ip: string): number | null {
  const parts = ip.split('.').map(Number);
  if (parts.length !== 4 || parts.some((p) => Number.isNaN(p) || p < 0 || p > 255)) return null;
  return ((parts[0] << 24) | (parts[1] << 16) | (parts[2] << 8) | parts[3]) >>> 0;
}

function isIpv4InCidr(ip: string, cidr: string): boolean {
  const [range, bitsStr] = cidr.split('/');
  const bits = Number(bitsStr);
  const ipLong = ipv4ToLong(ip);
  const rangeLong = ipv4ToLong(range);
  if (ipLong === null || rangeLong === null) return false;
  const mask = bits === 0 ? 0 : (~0 << (32 - bits)) >>> 0;
  return (ipLong & mask) === (rangeLong & mask);
}

// Loopback, RFC1918 private ranges, link-local, CGNAT, and other reserved/special-use ranges.
const BLOCKED_IPV4_RANGES = [
  '0.0.0.0/8',
  '10.0.0.0/8',
  '100.64.0.0/10',
  '127.0.0.0/8',
  '169.254.0.0/16',
  '172.16.0.0/12',
  '192.0.0.0/24',
  '192.168.0.0/16',
  '198.18.0.0/15',
  '224.0.0.0/4',
  '240.0.0.0/4',
  '255.255.255.255/32',
];

function isPrivateOrReservedIpv4(ip: string): boolean {
  return BLOCKED_IPV4_RANGES.some((cidr) => isIpv4InCidr(ip, cidr));
}

function isPrivateOrReservedIpv6(ip: string): boolean {
  const normalized = ip.toLowerCase();
  if (normalized === '::1' || normalized === '::') return true;
  if (/^fe[89ab][0-9a-f]:/.test(normalized)) return true; // link-local fe80::/10
  if (/^f[cd][0-9a-f]{2}:/.test(normalized)) return true; // unique local fc00::/7
  const mapped = normalized.match(/^::ffff:(\d+\.\d+\.\d+\.\d+)$/);
  if (mapped) return isPrivateOrReservedIpv4(mapped[1]);
  return false;
}

export function isPrivateOrReservedIp(ip: string): boolean {
  if (ip.includes(':')) return isPrivateOrReservedIpv6(ip);
  return isPrivateOrReservedIpv4(ip);
}

export interface SafeUrlCheckResult {
  hostname: string;
  resolvedIps: string[];
}

/**
 * Validates a URL against protocol, hostname and DNS-resolved-IP allowlists.
 * Re-run this for the initial URL AND every redirect hop (DNS rebinding: a
 * hostname can resolve to a public IP at check-time and a private one at
 * request-time, so re-resolving per hop is required, not optional).
 */
export async function assertUrlIsSafe(rawUrl: string): Promise<SafeUrlCheckResult> {
  let parsed: URL;
  try {
    parsed = new URL(rawUrl);
  } catch {
    throw new SsrfError(ERROR_CODES.INVALID_URL, 'The URL is not valid.');
  }

  if (parsed.protocol !== 'http:' && parsed.protocol !== 'https:') {
    throw new SsrfError(ERROR_CODES.UNSUPPORTED_PROTOCOL, 'Only http and https URLs are allowed.');
  }

  const hostname = parsed.hostname.toLowerCase();
  if (BLOCKED_HOSTNAMES.has(hostname)) {
    throw new SsrfError(ERROR_CODES.SSRF_BLOCKED, 'This URL cannot be accessed for security reasons.');
  }

  const bareHost = hostname.replace(/^\[|\]$/g, '');
  if (/^\d+\.\d+\.\d+\.\d+$/.test(bareHost) || bareHost.includes(':')) {
    if (isPrivateOrReservedIp(bareHost)) {
      throw new SsrfError(ERROR_CODES.SSRF_BLOCKED, 'This URL cannot be accessed for security reasons.');
    }
  }

  let addresses: string[];
  try {
    const results = await dns.lookup(hostname, { all: true, verbatim: true });
    addresses = results.map((r) => r.address);
  } catch {
    throw new SsrfError(ERROR_CODES.DNS_RESOLUTION_FAILED, 'The domain name could not be resolved.');
  }

  if (addresses.length === 0) {
    throw new SsrfError(ERROR_CODES.DNS_RESOLUTION_FAILED, 'The domain name could not be resolved.');
  }

  for (const address of addresses) {
    if (isPrivateOrReservedIp(address)) {
      throw new SsrfError(ERROR_CODES.SSRF_BLOCKED, 'This URL cannot be accessed for security reasons.');
    }
  }

  return { hostname, resolvedIps: addresses };
}
