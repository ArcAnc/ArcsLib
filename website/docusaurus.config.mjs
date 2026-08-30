import {themes as prismThemes} from 'prism-react-renderer';

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'PulseLib',
  tagline: 'Animation library for Minecraft mods',
  favicon: 'img/logo.svg',

  url: 'https://arcanc.github.io',
  baseUrl: '/ArcsLib/',
  organizationName: 'ArcAnc',
  projectName: 'ArcsLib',
  trailingSlash: true,

  onBrokenLinks: 'throw',
  markdown: {
    hooks: {
      onBrokenMarkdownLinks: 'warn',
    },
  },

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      {
        docs: {
          path: './docs',
          routeBasePath: '/',
          sidebarPath: './sidebars.js',
          lastVersion: 'current',
          versions: {
            current: {
              label: '26.2',
              path: '26.2',
            },
          },
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      },
    ],
  ],

  themeConfig: {
    image: 'img/logo.svg',
    navbar: {
      title: 'PulseLib',
      logo: {
        alt: 'PulseLib logo',
        src: 'img/logo.svg',
        href: '/26.2/',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'docs',
          position: 'left',
          label: 'Documentation',
        },
        {
          type: 'docsVersionDropdown',
          position: 'left',
        },
        {
          href: 'https://github.com/ArcAnc/ArcsLib',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Community',
          items: [
            {
              label: 'Discord',
              href: 'https://discord.gg/cwydhvYb2M',
            },
          ],
        },
        {
          title: 'More',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/ArcAnc/ArcsLib',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} ArcAnc. Built with Docusaurus.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['java', 'json'],
    },
  },
};

export default config;
